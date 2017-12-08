package uk.me.desiderio.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import uk.me.desiderio.popularmovies.data.DataUtils;
import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.data.MoviesContract;
import uk.me.desiderio.popularmovies.data.MoviesContract.FavoritessEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.MoviesEntry;
import uk.me.desiderio.popularmovies.data.MoviesCursorWrapper;
import uk.me.desiderio.popularmovies.network.ConnectivityUtils;
import uk.me.desiderio.popularmovies.network.ConnectivityUtils.ConnectivityState;
import uk.me.desiderio.popularmovies.network.MovieFeedType;
import uk.me.desiderio.popularmovies.task.MoviesIntentService;
import uk.me.desiderio.popularmovies.task.MoviesRequestTasks;
import uk.me.desiderio.popularmovies.view.ViewUtils;
import uk.me.desiderio.popularmovies.view.ViewUtils.ViewDataAvailability;

import static uk.me.desiderio.popularmovies.network.ConnectivityUtils.CONNECTED;
import static uk.me.desiderio.popularmovies.network.ConnectivityUtils.DISCONNECTED;
import static uk.me.desiderio.popularmovies.network.MovieFeedType.FAVORITES_MOVIES_FEED;
import static uk.me.desiderio.popularmovies.network.MovieFeedType.FeedType;
import static uk.me.desiderio.popularmovies.network.MovieFeedType.POPULAR_MOVIES_FEED;
import static uk.me.desiderio.popularmovies.network.MovieFeedType.TOP_RATED_MOVIES_FEED;
import static uk.me.desiderio.popularmovies.view.ViewUtils.FLAG_FRESH_VIEW_DATA;
import static uk.me.desiderio.popularmovies.view.ViewUtils.FLAG_NO_VIEW_DATA;
import static uk.me.desiderio.popularmovies.view.ViewUtils.FLAG_STALE_VIEW_DATA;
import static uk.me.desiderio.popularmovies.view.ViewUtils.hasAnyDataToShow;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        MovieAdapter.OnItemClickListener,
ConnectivityManager.OnNetworkActiveListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int MOVIE_LOADER_ID = 300;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VISIBLE, INVISIBLE})
    @interface EmptyStateViewVisibility {}
    private static final int VISIBLE = 1;
    private static final int INVISIBLE = 0;

    private MovieAdapter adapter;
    private View emptyStateView;
    private TextView emptyStateTextView;

    @FeedType
    private String currentFeedType;

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // instantiates & sets RecyclerView
        RecyclerView recyclerView = findViewById(R.id.movie_list_recycler_view);
        emptyStateView = findViewById(R.id.empty_state_view);
        emptyStateTextView = (TextView) emptyStateView.findViewById(R.id.empty_state_text_view);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.main_list_view_column_span));

        adapter = new MovieAdapter();
        adapter.setOnItemClickListener(this);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        currentFeedType = POPULAR_MOVIES_FEED;
        Bundle initialLoaderBundle = getMovieLoaderBundle(POPULAR_MOVIES_FEED);
        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, initialLoaderBundle, this);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.addDefaultNetworkActiveListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.popular_movies_menu_item:
                changeFeedType(POPULAR_MOVIES_FEED);
                return true;
            case R.id.top_rated_movies_menu_item:
                changeFeedType(TOP_RATED_MOVIES_FEED);
                return true;
            case R.id.favorites_movies_menu_item:
                changeFeedType(FAVORITES_MOVIES_FEED);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetLoaderWithFeedType() {
        adapter.swapCursor(null);
        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID,
                getMovieLoaderBundle(currentFeedType),
                this);
    }

    @NonNull
    private Bundle getMovieLoaderBundle(@FeedType String feedType) {
        Bundle bundle = new Bundle();
        bundle.putString(MoviesIntentService.EXTRA_FEED_TYPE, feedType);
        return bundle;
    }

    private void setEmptyStateViewText(@NonNull @FeedType String feedType) {
        switch (feedType) {
            case MovieFeedType.FAVORITES_MOVIES_FEED:
                emptyStateTextView.setText(R.string.main_empty_state_favorite_message);
                break;
            default:
                emptyStateTextView.setText(R.string.main_empty_state_default_message);
        }
    }


    private void setEmptyStateViewVisibility(@NonNull @EmptyStateViewVisibility int visibility, @NonNull @FeedType String feedType) {
        switch ((visibility)) {
            case VISIBLE:
                setEmptyStateViewText(feedType);
                emptyStateView.setVisibility(View.VISIBLE);
                break;
            case INVISIBLE:
                emptyStateView.setVisibility(View.GONE);
                break;
        }
    }

    @FeedType
    private void changeFeedType(@NonNull String feedType) {
        currentFeedType = feedType;
        resetLoaderWithFeedType();
    }

    private void showSnack(@NonNull @ConnectivityState int connectivityState) {
        // shows snack bar displaying different message depending on connection state
        View anchorView = findViewById(R.id.movie_list_recycler_view);
        final Snackbar bar = ViewUtils.getSnackbar(connectivityState, anchorView, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetLoaderWithFeedType();
            }
        });
        bar.show();
    }

    @NonNull
    @ConnectivityState
    private int getConnectivityState() {
        return ConnectivityUtils.checkConnectivity(this);
    }

    @Override
    public void onNetworkActive() {
        if(!adapter.hasData()) {
            showSnack(CONNECTED);
        }
    }

    @Override
    public void onItemClick(Movie movie, View sharedView) {
        Intent intent = new Intent(this, DetailsActivity.class);
        // passes data to the DetailsActivity
        intent.putExtra(DetailsActivity.EXTRA_MOVIE, movie);
        // sets shared element activity transition
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, sharedView, "movie_poster_transition_name");
        startActivity(intent, options.toBundle());
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, final Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            private final ContentObserver obs = new ContentObserver(new Handler()) {
                @Override
                public boolean deliverSelfNotifications() {
                    return true;
                }

                @Override
                public void onChange(boolean selfChange) {
                    onContentChanged();
                }
            };

            Cursor cursor;

            @Override
            protected void onStartLoading() {
                // makes sure that the favorite view updates after removing movie from the favorite list
                boolean hasChanged = takeContentChanged();
                if (cursor == null || hasChanged) {
                    setEmptyStateViewVisibility(VISIBLE, currentFeedType);
                    forceLoad();
                } else {
                    deliverResult(cursor);
                }
            }

            @Override
            public Cursor loadInBackground() {
                String feedType = args.getString(MoviesIntentService.EXTRA_FEED_TYPE);

                Cursor cursor = null;
                Cursor cursorWrapper = null;

                switch (feedType) {
                    case MovieFeedType.FAVORITES_MOVIES_FEED:
                        cursor = getFavoritesCursor();
                        break;

                    case MovieFeedType.POPULAR_MOVIES_FEED:
                    case MovieFeedType.TOP_RATED_MOVIES_FEED:
                        cursor = getMoviesCursor(feedType);
                        break;
                }
                if(cursor != null) {
                    cursorWrapper = new MoviesCursorWrapper(cursor, feedType);
                    cursorWrapper.registerContentObserver(obs);
                }
                return cursorWrapper;
            }

            @Override
            public void deliverResult(Cursor cursor) {
                this.cursor = cursor;
                super.deliverResult(cursor);
            }

            private Cursor getMoviesCursor(String feedType) {
                String selection = MoviesEntry.COLUMN_FEED_TYPE + " = ?";
                String[] selectinArgs = {feedType};

                Cursor cursor = getContentResolver().query(MoviesEntry.CONTENT_URI,
                        null,
                        selection,
                        selectinArgs,
                        null);
                return cursor;
            }

            private Cursor getFavoritesCursor() {
                Cursor cursor = getContentResolver().query(FavoritessEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
                return cursor;
            }
        };
    }

    private void onLoadFavoriteFinished(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            adapter.swapCursor(cursor);
            setEmptyStateViewVisibility(INVISIBLE, currentFeedType);
        }
    }

    private void onLoadMoviesFinished(Cursor cursor, @FeedType String feedType) {
        if(hasAnyDataToShow(MainActivity.this, cursor)) {
            adapter.swapCursor(cursor);
            setEmptyStateViewVisibility(INVISIBLE, currentFeedType);
        } else {
            requestServerData(feedType);
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        MoviesCursorWrapper cursorWrapper = (MoviesCursorWrapper) data;
        String feedType = cursorWrapper.getFeedType();

        switch (feedType) {
            case POPULAR_MOVIES_FEED:
                onLoadMoviesFinished(data, feedType);
                break;
            case TOP_RATED_MOVIES_FEED:
                onLoadMoviesFinished(data, feedType);
                break;
            case FAVORITES_MOVIES_FEED:
                onLoadFavoriteFinished(data);
                break;
        }
    }

    public void requestServerData(@NonNull @FeedType String feedType) {
        int connectivityState = getConnectivityState();

        switch (connectivityState) {
            case CONNECTED:
                Log.d(TAG, "requestServerData: device IS connected : feed type " + feedType);
                Intent intent = new Intent(this, MoviesIntentService.class);
                intent.setAction(MoviesRequestTasks.ACTION_REQUEST_MOVIE_DATA);
                intent.putExtra(MoviesIntentService.EXTRA_FEED_TYPE, feedType);
                startService(intent);
                break;
            case DISCONNECTED:
                Log.d(TAG, "requestServerData: device is NOT connected: feed type " + feedType);
                showSnack(DISCONNECTED);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
        setEmptyStateViewVisibility(VISIBLE, currentFeedType);
    }
}