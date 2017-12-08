package uk.me.desiderio.popularmovies;


import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.data.MoviesContract.FavoritessEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.ReviewEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.TrailerEntry;
import uk.me.desiderio.popularmovies.network.ConnectivityUtils;
import uk.me.desiderio.popularmovies.network.MovieDatabaseRequestUtils;
import uk.me.desiderio.popularmovies.task.MoviesIntentService;
import uk.me.desiderio.popularmovies.task.MoviesRequestTasks;
import uk.me.desiderio.popularmovies.view.ViewUtils;

import static uk.me.desiderio.popularmovies.network.ConnectivityUtils.CONNECTED;
import static uk.me.desiderio.popularmovies.network.ConnectivityUtils.DISCONNECTED;
import static uk.me.desiderio.popularmovies.view.ViewUtils.hasAnyDataToShow;

public class DetailsActivity extends AppCompatActivity
        implements DetailsAdapter.OnItemClickListener, ConnectivityManager.OnNetworkActiveListener {

    public static final String TAG = DetailsActivity.class.getSimpleName();

    public static final String EXTRA_MOVIE = "extra_movie";

    private static final int TRAILERS_LOADER_ID = 500;
    private static final int REVIEWS_LOADER_ID = 600;


    private RecyclerView recyclerView;
    private DetailsAdapter adapter;
    private Button favoriteButton;
    private ScrollView scrollView;

    private TrailerLoaderCallbacks trailerLoaderCallbacks;
    private ReviewLoaderCallbacks reviewLoaderCallbacks;

    private Bundle movieBundle;
   /* private Movie movie;
    private String movieId;*/

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        movieBundle = intent.getExtras();
        Movie movie = movieBundle.getParcelable(EXTRA_MOVIE);

        Uri uri = MovieDatabaseRequestUtils.getMoviePosterUri(movie.getPosterURLPathString());
        String releaseYear = movie.getDate().substring(0, movie.getDate().indexOf("-"));

        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView dateTextView = findViewById(R.id.dateTextView);
        TextView voteTextView = findViewById(R.id.voteTextView);
        TextView synopsisTextView = findViewById(R.id.synopsisTextView);
        ImageView posterImageView = findViewById(R.id.detailsPosterImageView);

        scrollView = findViewById(R.id.details_scroll_view);
        // Wait until my scrollView is ready to reset it to the top of the view
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollView.fullScroll(View.FOCUS_UP);
            }
        });

        favoriteButton = findViewById(R.id.detail_favorite_button);


        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleFavorite(getMovieIdString(movieBundle));
            }
        });

        Picasso.with(this).load(uri).into(posterImageView);

        titleTextView.setText(movie.getTitle());
        dateTextView.setText(releaseYear);
        voteTextView.setText(getVoteAverageString(movie.getVoteAverage()));
        synopsisTextView.setText(movie.getSynopsis());

        // instantiates & sets RecyclerView
        recyclerView = findViewById(R.id.detail_list_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        adapter = new DetailsAdapter(this);
        adapter.registerOnItemClickListener(this);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        adapter.resetData();

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.addDefaultNetworkActiveListener(this);

        trailerLoaderCallbacks = new TrailerLoaderCallbacks();
        reviewLoaderCallbacks = new ReviewLoaderCallbacks();

        getSupportLoaderManager().initLoader(TRAILERS_LOADER_ID, movieBundle, trailerLoaderCallbacks);
        getSupportLoaderManager().initLoader(REVIEWS_LOADER_ID, movieBundle, reviewLoaderCallbacks);
    }

    private void restartLoader() {
        getSupportLoaderManager().restartLoader(TRAILERS_LOADER_ID, movieBundle, trailerLoaderCallbacks);
        getSupportLoaderManager().restartLoader(TRAILERS_LOADER_ID, movieBundle, trailerLoaderCallbacks);

    }
    @Override
    protected void onResume() {
        super.onResume();

        String movieId = getMovieIdString(movieBundle);

        boolean isFavorite= isMovieFavorite(movieId);
        setButtonLabel(isFavorite);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // stars return activity animation on the Up/Home button been selected
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.share_menu_item:
                sendShareIntent();
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
                getYouTubeUri(trailerKey));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    private void sendShareIntent() {
        String key = adapter.getVideoSharingKey();
        if(key != null) {

        String title = String.format(getString(R.string.share_title), getMovie().getTitle());
        String urlString = getYouTubeUrlString(key);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, urlString);

        startActivity(Intent.createChooser(intent, title));
        } else {
            Toast.makeText(this, R.string.share_error_message, Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getYouTubeUri(String key) {
        return Uri.parse(getYouTubeUrlString(key));
    }

    private String getYouTubeUrlString (String key) {
        return getString(R.string.youtube_url_string) + key;
    }

    private String getVoteAverageString(double vote) {
        return String.valueOf(vote) + getString(R.string.vote_average_denominator_suffix);
    }

    private Movie getMovie() {
        return movieBundle.getParcelable(EXTRA_MOVIE);
    }

    private String getMovieIdString(Bundle bundle) {
        Movie movie = bundle.getParcelable(EXTRA_MOVIE);
        if(movie != null) {
            int movieId = movie.getId();
            return String.valueOf(movieId);
        }
        return null;
    }

    private void setButtonLabel(boolean isFavorite) {
        if(isFavorite) {
            favoriteButton.setText(R.string.favorites_button_remove_string);
        } else {
            favoriteButton.setText(R.string.favorites_button_add_string);
        }
    }

    private boolean isMovieFavorite(String movieId) {
        String selection = FavoritessEntry.COLUMN_MOVIE_ID + " = ?";
        String[] selectionArgs = {movieId};
        Cursor cursor = getContentResolver().query(FavoritessEntry.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null);

        if(cursor != null) {
            boolean isMovieInFavorites = cursor.getCount() != 0;
            cursor.close();
            return isMovieInFavorites;
        }

        return false;
    }


    private void toggleFavorite(String movieId) {
        boolean isFavourite = isMovieFavorite(movieId);

        if(isFavourite) {
            String whereClause = FavoritessEntry.COLUMN_MOVIE_ID + " = ?";
            String[] whereArgs = {movieId};
            int deleteItem = getContentResolver().delete(FavoritessEntry.CONTENT_URI,
                    whereClause,
                    whereArgs);

            if(deleteItem > 0) {
                setButtonLabel(false);
                Log.d(TAG, "Favorite deleted : " + deleteItem);
            }
        } else {
            Movie movie = getMovie();

            ContentValues values = new ContentValues();
            values.put(FavoritessEntry.COLUMN_MOVIE_ID, movieId);
            values.put(FavoritessEntry.COLUMN_TITLE, movie.getTitle());
            values.put(FavoritessEntry.COLUMN_DATE, movie.getDate());
            values.put(FavoritessEntry.COLUMN_SYNOPSIS, movie.getSynopsis());
            values.put(FavoritessEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
            values.put(FavoritessEntry.COLUMN_POSTER_URL, movie.getPosterURLPathString());

            Uri uri = getContentResolver().insert(FavoritessEntry.CONTENT_URI,
                    values);

            if(uri != null) {
                setButtonLabel(true);
                Log.d(TAG, "Insert favorite : " + movie.getId());
            }
        }

        getContentResolver().notifyChange(FavoritessEntry.CONTENT_URI, null, false);

    }

    private void showSnack(@ConnectivityUtils.ConnectivityState int connectivityState) {
        View anchorView = findViewById(R.id.details_scroll_view);
        final Snackbar bar = ViewUtils.getSnackbar(connectivityState, anchorView, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartLoader();
            }
        });
        bar.show();
    }

    @ConnectivityUtils.ConnectivityState
    private int getConnectivityState() {
        return ConnectivityUtils.checkConnectivity(this);
    }

    @Override
    public void onNetworkActive() {
        if(!adapter.hasData()) {
            showSnack(CONNECTED);
        }
    }


    private void requestReviewData(Context context, String movieId) {
        int connectionState = getConnectivityState();

        if (connectionState == CONNECTED) {
            Intent intent = new Intent(context, MoviesIntentService.class);
            intent.setAction(MoviesRequestTasks.ACTION_REQUEST_REVIEW_DATA);
            intent.putExtra(MoviesIntentService.EXTRA_MOVIE_ID, movieId);
            startService(intent);
        } else {
            showSnack(DISCONNECTED);
        }
    }

    private void requestTrailerData(Context context, String movieId) {
        int connectionState = getConnectivityState();

        if (connectionState == CONNECTED) {
            Intent intent = new Intent(context, MoviesIntentService.class);
            intent.setAction(MoviesRequestTasks.ACTION_REQUEST_TRAILER_DATA);
            intent.putExtra(MoviesIntentService.EXTRA_MOVIE_ID, movieId);
            startService(intent);
        } else {
            showSnack(DISCONNECTED);

        }
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
            if (hasAnyDataToShow(DetailsActivity.this, reviewsCursor)) {
                adapter.swapReviewsCursor(reviewsCursor);
            } else {
                requestReviewData(DetailsActivity.this, movieId);
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
            if (hasAnyDataToShow(DetailsActivity.this, trailersCursor)) {
                Log.d(TAG, "puerco : trailer: updating cursor -------- ");
                adapter.swapTrailersCursor(trailersCursor);
            } else {
                Log.d(TAG, "puerco : trailer: Reloading data.....");
                requestTrailerData(DetailsActivity.this, movieId);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter.swapTrailersCursor(null);
        }
    }

}
