package uk.me.desiderio.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.net.URL;
import java.util.List;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.data.MovieDBHelper;
import uk.me.desiderio.popularmovies.data.MoviesContract.MoviesEntry;
import uk.me.desiderio.popularmovies.network.MovieDatabaseRequestUtils;
import uk.me.desiderio.popularmovies.task.AsyncTaskCompleteListener;
import uk.me.desiderio.popularmovies.task.MovieRequestAsyncTask;

public class MainActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener,
        AsyncTaskCompleteListener<List<Movie>> {

    // TODO implement suggesiton about connection receiver

    private MovieAdapter adapter;
    private View emptyStateView;
    private SQLiteDatabase database;

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

        // show empty state view initialy
        showEmptyStateView(true);

        MovieDBHelper helper = new MovieDBHelper(this);
        database = helper.getWritableDatabase();

        // requests data & populates view
        requestData(MovieDatabaseRequestUtils.getPopularMoviesUrl());
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
                requestData(MovieDatabaseRequestUtils.getPopularMoviesUrl());
                return true;
            case R.id.top_rated_movies_menu_item:
                requestData(MovieDatabaseRequestUtils.getTopRatedMoviesUrl());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showEmptyStateView(boolean isVisible) {
        if (isVisible) {
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private void requestData(URL url) {
        if (isConnected()) {
            new MovieRequestAsyncTask(this).execute(url);
        } else {
            Toast.makeText(this, getString(R.string.no_connection_message), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onItemClick(Movie movie, View sharedView) {
        Intent intent = new Intent(this, DetailsActivity.class);
        // passes data to the DetailsActivity
        intent.putExtra(DetailsActivity.EXTRA_MOVIE, movie);
        //
        intent.putExtra(DetailsActivity.EXTRA_MOVIE_DB_ID, movie.getId());
        // sets shared element activity transition
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, sharedView, "movie_poster_transition_name");
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onTaskComplete(List<Movie> movies) {
        // shows different view state based on data presence
        if (movies != null && movies.size() > 0) {
            // TODO refractor parser when provider is in place. See if Movie object can be removed
            insertAllMoviesInDatabase(movies);
            Cursor cursor = getAllMovies();
            Log.d("MAIN", "===== DB query count >> " + cursor.getCount());
            adapter.setData(cursor);
            showEmptyStateView(false);
        } else {
            adapter.setData(null);
            showEmptyStateView(true);
        }
    }

    private void insertAllMoviesInDatabase(List<Movie> movies) {
        for(Movie movie : movies) {
            insertMovieInDatabase(movie);
        }
    }

    private void insertMovieInDatabase(Movie movie) {
        ContentValues values = new ContentValues();

        values.put(MoviesEntry.COLUMN_MOVIE_ID, movie.getId());
        values.put(MoviesEntry.COLUMN_TITLE, movie.getTitle());
        values.put(MoviesEntry.COLUMN_DATE, movie.getDate());
        values.put(MoviesEntry.COLUMN_SYNOPSIS, movie.getSynopsis());
        values.put(MoviesEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        values.put(MoviesEntry.COLUMN_POSTER_URL, movie.getPosterURLPathString());

        long inserts = database.insert(MoviesEntry.TABLE_NAME, "", values);
        Log.d("MAIN", "===== DB Insert >> " + inserts);
    }

    private Cursor getAllMovies() {
        return database.query(MoviesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}