package uk.me.desiderio.popularmovies;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.data.MoviesContract.FavoritessEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.MoviesEntry;
import uk.me.desiderio.popularmovies.data.MoviesCursorWrapper;
import uk.me.desiderio.popularmovies.network.MovieFeedType;
import uk.me.desiderio.popularmovies.task.MoviesIntentService;
import uk.me.desiderio.popularmovies.task.MoviesRequestTasks;

import static uk.me.desiderio.popularmovies.network.MovieFeedType.*;
import static uk.me.desiderio.popularmovies.network.MovieFeedType.POPULAR_MOVIES_FEED;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        MovieAdapter.OnItemClickListener {

    private static final int MOVIE_LOADER_ID = 300;

    private MovieAdapter adapter;
    private View emptyStateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // instantiates & sets RecyclerView
        RecyclerView recyclerView = findViewById(R.id.movie_list_recycler_view);
        emptyStateView = findViewById(R.id.empty_state_view);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.main_list_view_column_span));

        adapter = new MovieAdapter();
        adapter.setOnItemClickListener(this);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        Bundle initialLoaderBundle = getMovieLoaderBundle(POPULAR_MOVIES_FEED);
        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, initialLoaderBundle, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_movie_order, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.popular_movies_menu_item:
                resetLoaderWithFeedType(POPULAR_MOVIES_FEED);
                return true;
            case R.id.top_rated_movies_menu_item:
                resetLoaderWithFeedType(TOP_RATED_MOVIES_FEED);
                return true;
            case R.id.favorites_movies_menu_item:
                resetLoaderWithFeedType(FAVORITES_MOVIES_FEED);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void resetLoaderWithFeedType(@FeedType String feedType) {
        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID,
                getMovieLoaderBundle(feedType),
                this);
    }

    private Bundle getMovieLoaderBundle(@FeedType String feedType) {
        Bundle bundle = new Bundle();
        bundle.putString(MoviesIntentService.EXTRA_FEED_TYPE, feedType);
        return bundle;
    }

    private void showEmptyStateView(boolean isVisible) {
        if (isVisible) {
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            emptyStateView.setVisibility(View.GONE);
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
        // TODO revise which fields are used then create Projection + cursor fields numbers consttant
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
                    showEmptyStateView(true);
                    forceLoad();
                } else {
                    deliverResult(cursor);
                }
            }

            @Override
            public Cursor loadInBackground() {
                @FeedType String feedType = args.getString(MoviesIntentService.EXTRA_FEED_TYPE);

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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // data.getCount > 0
        if (data != null && data.getCount() > 0) {
            adapter.swapCursor(data);
            showEmptyStateView(false);
        } else {
            MoviesCursorWrapper cursorWrapper = (MoviesCursorWrapper) data;
            String feedType = cursorWrapper.getFeedType();

            // only request data for popular and top rate movies
            if(feedType.equals(MovieFeedType.POPULAR_MOVIES_FEED) ||
                    feedType.equals(MovieFeedType.TOP_RATED_MOVIES_FEED)) {
                Intent intent = new Intent(this, MoviesIntentService.class);
                intent.setAction(MoviesRequestTasks.ACTION_REQUEST_MOVIE_DATA);
                intent.putExtra(MoviesIntentService.EXTRA_FEED_TYPE, feedType);
                startService(intent);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
        showEmptyStateView(true);
    }
}