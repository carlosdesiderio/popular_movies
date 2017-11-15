package uk.me.desiderio.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.data.MovieReview;
import uk.me.desiderio.popularmovies.data.MovieTrailer;
import uk.me.desiderio.popularmovies.network.MovieDatabaseJSONParserUtils;
import uk.me.desiderio.popularmovies.network.MovieDatabaseRequestUtils;

public class DetailsActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "extra_movie";
    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        movie = intent.getParcelableExtra(EXTRA_MOVIE);
        Uri uri = MovieDatabaseRequestUtils.getMoviePosterUri(movie.getPosterURLPathString());
        String releaseYear = movie.getDate().substring(0, movie.getDate().indexOf("-"));

        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView dateTextView = findViewById(R.id.dateTextView);
        TextView voteTextView = findViewById(R.id.voteTextView);
        TextView synopsisTextView = findViewById(R.id.synopsisTextView);
        ImageView posterImageView = findViewById(R.id.detailsPosterImageView);

        Picasso.with(this).load(uri).into(posterImageView);

        titleTextView.setText(movie.getTitle());
        dateTextView.setText(releaseYear);
        voteTextView.setText(getVoteAverageString(movie.getVoteAverage()));
        synopsisTextView.setText(movie.getSynopsis());

        new MovieTrailersRequestAsyncTask().execute(String.valueOf(movie.getId()));
        new MovieReviewsRequestAsyncTask().execute(String.valueOf(movie.getId()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // stars return activity animation on the Up/Home button been selected
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getVoteAverageString(double vote) {
        return String.valueOf(vote) + getString(R.string.vote_average_denominator_suffix);
    }

    public class MovieTrailersRequestAsyncTask extends AsyncTask<String, Void, List<MovieTrailer>> {

        @Override
        protected List<MovieTrailer> doInBackground(String... movieIds) {
            URL url = MovieDatabaseRequestUtils.getMovieTrailersUrl(movieIds[0]);
            try {
                String response = MovieDatabaseRequestUtils.getResponseFromHttpUrl(url);
                return MovieDatabaseJSONParserUtils.parseTrailerJsonString(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MovieTrailer> movieTrailers) {
            movie.addTrailers(movieTrailers);
        }
    }


    public class MovieReviewsRequestAsyncTask extends AsyncTask<String, Void, List<MovieReview>> {

        @Override
        protected List<MovieReview> doInBackground(String... movieIds) {
            URL url = MovieDatabaseRequestUtils.getMovieReviewsUrl(movieIds[0]);
            try {
                String response = MovieDatabaseRequestUtils.getResponseFromHttpUrl(url);
                return MovieDatabaseJSONParserUtils.parseReviewsJsonString(response);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<MovieReview> movieReviews) {
            movie.addReviews(movieReviews);
        }
    }

}
