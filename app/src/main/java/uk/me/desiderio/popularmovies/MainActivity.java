package uk.me.desiderio.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.net.URL;
import java.util.List;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.network.MovieDatabaseRequestUtils;
import uk.me.desiderio.popularmovies.task.AsyncTaskCompleteListener;
import uk.me.desiderio.popularmovies.task.MovieRequestAsyncTask;

public class MainActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener,
        AsyncTaskCompleteListener<List<Movie>> {

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

        // show empty state view initialy
        showEmptyStateView(true);

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
        // sets shared element activity transition
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, sharedView, "movie_poster_transition_name");
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onTaskComplete(List<Movie> movies) {
        // shows different view state based on data presence
        if (movies != null && movies.size() > 0) {
            adapter.setData(movies);
            showEmptyStateView(false);
        } else {
            adapter.setData(null);
            showEmptyStateView(true);
        }
    }
}