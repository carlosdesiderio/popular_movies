package uk.me.desiderio.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.data.MovieReview;
import uk.me.desiderio.popularmovies.data.MovieTrailer;
import uk.me.desiderio.popularmovies.network.MovieDatabaseRequestUtils;
import uk.me.desiderio.popularmovies.task.MovieReviewsRequestAsyncTask;
import uk.me.desiderio.popularmovies.task.MovieTrailersRequestAsyncTask;

public class DetailsActivity extends AppCompatActivity
        implements MovieReviewsRequestAsyncTask.AsyncTaskCompleteListener,
        MovieTrailersRequestAsyncTask.AsyncTaskCompleteListener,
        DetailsAdapter.OnItemClickListener {


    public static final String EXTRA_MOVIE = "extra_movie";
    private Movie movie;
    private DetailsAdapter adapter;

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

        // instantiates & sets RecyclerView
        RecyclerView recyclerView = findViewById(R.id.detail_list_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        adapter = new DetailsAdapter(this);
        adapter.registerListener(this);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        //TODO check connection before making request
        new MovieTrailersRequestAsyncTask(this).execute(String.valueOf(movie.getId()));
        new MovieReviewsRequestAsyncTask(this).execute(String.valueOf(movie.getId()));
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


    @Override
    public void onTrailerTaskComplete(List<MovieTrailer> trailers) {
        movie.addTrailers(trailers);
        adapter.addTrailerData(trailers);
    }

    @Override
    public void onReviewTaskComplete(List<MovieReview> reviews) {
        movie.addReviews(reviews);
        adapter.addReviewData(reviews);
    }


    @Override
    public void onReviewSelected(MovieReview review) {
        Uri uri = Uri.parse(review.getUrl());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onTrailerSelected(MovieTrailer trailer) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailer.getKey()));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.youtube_url_string) + trailer.getKey()));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }

    }
}
