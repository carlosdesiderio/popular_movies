package uk.me.desiderio.popularmovies;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.data.MoviesContract.ReviewEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.TrailerEntry;
import uk.me.desiderio.popularmovies.network.MovieDatabaseRequestUtils;
import uk.me.desiderio.popularmovies.task.MoviesIntentService;
import uk.me.desiderio.popularmovies.task.MoviesRequestTasks;

public class DetailsActivity extends AppCompatActivity
        implements DetailsAdapter.OnItemClickListener {

    public static final String EXTRA_MOVIE = "extra_movie";

    private static final int TRAILERS_LOADER_ID = 500;
    private static final int REVIEWS_LOADER_ID = 600;

    private DetailsAdapter adapter;
    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        // TODO check if this have to be removed and rely on provider only
        Bundle movieBundle = intent.getExtras();
        movie = movieBundle.getParcelable(EXTRA_MOVIE);
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

        adapter.resetData();

        TrailerLoaderCallbacks trailerLoaderCallbacks = new TrailerLoaderCallbacks();
        ReviewLoaderCallbacks reviewLoaderCallbacks = new ReviewLoaderCallbacks();

        getSupportLoaderManager().initLoader(TRAILERS_LOADER_ID, movieBundle, trailerLoaderCallbacks);
        getSupportLoaderManager().initLoader(REVIEWS_LOADER_ID, movieBundle, reviewLoaderCallbacks);
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

    @Override
    public void onReviewSelected(String reviewUrlString) {
        Uri uri = Uri.parse(reviewUrlString);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onTrailerSelected(String trailerKey) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailerKey));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.youtube_url_string) + trailerKey));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    private String getVoteAverageString(double vote) {
        return String.valueOf(vote) + getString(R.string.vote_average_denominator_suffix);
    }

    private String getMovieIdString(Bundle bundle) {
        Movie movie = bundle.getParcelable(EXTRA_MOVIE);
        int movieId = movie.getId();
        return String.valueOf(movieId);
    }

    private class ReviewLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        String movieId;

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            movieId = getMovieIdString(bundle);
            String selection = ReviewEntry.COLUMN_MOVIES_FOREING_KEY+ " = ?";
            String[] selectionArgs = {
                    movieId
            };

            return new CursorLoader(getApplicationContext(),
                    ReviewEntry.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor reviewsCursor) {
            if(reviewsCursor != null && reviewsCursor.getCount() > 0) {
                adapter.swapReviewsCursor(reviewsCursor);
            } else {
                Intent intent = new Intent(DetailsActivity.this, MoviesIntentService.class);
                intent.setAction(MoviesRequestTasks.ACTION_REQUEST_REVIEW_DATA);
                intent.putExtra(MoviesIntentService.EXTRA_MOVIE_ID, movieId);
                startService(intent);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter.swapReviewsCursor(null);
        }
    }

    private class TrailerLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
            String movieId;

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            movieId = getMovieIdString(bundle);
            String selection = TrailerEntry.COLUMN_MOVIES_FOREING_KEY+ " = ?";
            String[] selectionArgs = {
                    movieId
            };

            return new CursorLoader(getApplicationContext(),
                    TrailerEntry.CONTENT_URI,
                    null,
                    selection,
                    selectionArgs,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor trailersCursor) {
            if(trailersCursor != null && trailersCursor.getCount() > 0) {
                adapter.swapTrailersCursor(trailersCursor);
            } else {
                Intent intent = new Intent(DetailsActivity.this, MoviesIntentService.class);
                intent.setAction(MoviesRequestTasks.ACTION_REQUEST_TRAILER_DATA);
                intent.putExtra(MoviesIntentService.EXTRA_MOVIE_ID, movieId);
                startService(intent);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter.swapTrailersCursor(null);
        }
    }
}
