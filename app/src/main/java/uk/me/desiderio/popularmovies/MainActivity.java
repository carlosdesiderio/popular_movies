package uk.me.desiderio.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.net.URL;
import java.util.List;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.network.MovieDatabaseJSONParserUtils;
import uk.me.desiderio.popularmovies.network.MovieDatabaseRequestUtils;

public class MainActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener {

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

        // hides empty state view
        showEmptyStateView(false);

        // requests data & populates view
        new MovieRequestAsyncTask().execute(MovieDatabaseRequestUtils.getPopularMoviesUrl());

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
                new MovieRequestAsyncTask().execute(MovieDatabaseRequestUtils.getPopularMoviesUrl());
                return true;
            case R.id.top_rated_movies_menu_item:
                new MovieRequestAsyncTask().execute(MovieDatabaseRequestUtils.getTopRatedMoviesUrl());
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

    public class MovieRequestAsyncTask extends AsyncTask<URL, Void, List<Movie>> {

        @Override
        protected List<Movie> doInBackground(URL... urls) {
            try {
                // requests data from movie db service
                String responseString = MovieDatabaseRequestUtils.getResponseFromHttpUrl(urls[0]);
                // parses json data into a list of Movie objects
                return MovieDatabaseJSONParserUtils.parseJsonString(responseString);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
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
}