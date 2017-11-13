package uk.me.desiderio.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.data.MovieDataUtils;

public class MainActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private View emptyStateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView =  findViewById(R.id.movie_list_recycler_view);
        emptyStateView = findViewById(R.id.empty_state_view);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

        MovieAdapter adapter = new MovieAdapter();
        adapter.setData(MovieDataUtils.getMovieMockData());
        adapter.setOnItemClickListener(this);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        showEmptyStateView(false);
    }

    private void showEmptyStateView(boolean isVisible) {
        if(isVisible) {
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            emptyStateView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(Movie movie) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.EXTRA_MOVIE, movie);
        startActivity(intent);
    }
}
