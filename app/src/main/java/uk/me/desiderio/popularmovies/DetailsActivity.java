package uk.me.desiderio.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.data.MovieDBHelper;
import uk.me.desiderio.popularmovies.data.MovieReview;
import uk.me.desiderio.popularmovies.data.MovieTrailer;
import uk.me.desiderio.popularmovies.data.MoviesContract;
import uk.me.desiderio.popularmovies.data.MoviesContract.ReviewEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.TrailerEntry;
import uk.me.desiderio.popularmovies.network.MovieDatabaseRequestUtils;
import uk.me.desiderio.popularmovies.task.MovieReviewsRequestAsyncTask;
import uk.me.desiderio.popularmovies.task.MovieTrailersRequestAsyncTask;

public class DetailsActivity extends AppCompatActivity
        implements MovieReviewsRequestAsyncTask.AsyncTaskCompleteListener,
        MovieTrailersRequestAsyncTask.AsyncTaskCompleteListener,
        DetailsAdapter.OnItemClickListener {

    public static final String EXTRA_MOVIE = "extra_movie";
    public static final String EXTRA_MOVIE_DB_ID = "extra_movie_database_id";

    private Movie movie;
    private int movieDatabaseId;
    private DetailsAdapter adapter;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        // TODO check if this have to be removed and rely on provider only
        movie = intent.getParcelableExtra(EXTRA_MOVIE);
        movieDatabaseId = intent.getIntExtra(EXTRA_MOVIE_DB_ID, -1);
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

        MovieDBHelper helper = new MovieDBHelper(this);
        database = helper.getWritableDatabase();
        requestData();
    }

    private void requestData() {
        //TODO check connection before making request
        adapter.resetData();
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
        // TODO revise if parser could insert data directly into database
        if(trailers == null) {
            return;
        }
        movie.addTrailers(trailers);
        for(MovieTrailer trailer : trailers) {
            insertTrailerInDatabase(trailer);
        }
        Cursor trailersCursor = getAllTrailers(String.valueOf(movieDatabaseId));
        adapter.addMovieTrailerData(trailersCursor);
    }

    @Override
    public void onReviewTaskComplete(List<MovieReview> reviews) {
        // TODO revise if parser could insert data directly into database
        if(reviews == null) {
            return;
        }
        movie.addReviews(reviews);
        for(MovieReview review : reviews) {
            insertReviewsInDatabase(review);
        }
        Cursor reviewsCursor = getAllReviews(String.valueOf(movieDatabaseId));
        adapter.addMovieReviewData(reviewsCursor);
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

    private void insertTrailerInDatabase(MovieTrailer trailer) {
        ContentValues values = new ContentValues();

        values.put(TrailerEntry.COLUMN_TRAILER_ID, trailer.getId());
        values.put(TrailerEntry.COLUMN_NAME, trailer.getName());
        values.put(TrailerEntry.COLUMN_KEY, trailer.getKey());
        values.put(TrailerEntry.COLUMN_MOVIES_FOREING_KEY, movieDatabaseId);

        long inserts = database.insert(TrailerEntry.TABLE_NAME, "", values);
        Log.d("DETAILS", "===== TRAILERS DB Insert >> " + inserts);
    }

    private void insertReviewsInDatabase(MovieReview review) {
        ContentValues values = new ContentValues();

        values.put(ReviewEntry.COLUMN_REVIEW_ID, review.getId());
        values.put(ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
        values.put(ReviewEntry.COLUMN_CONTENT, review.getContent());
        values.put(ReviewEntry.COLUMN_URL, review.getUrl());
        values.put(ReviewEntry.COLUMN_MOVIES_FOREING_KEY, movieDatabaseId);

        long inserts = database.insert(ReviewEntry.TABLE_NAME, "", values);
        Log.d("DETAILS", "===== REVIEWS DB Insert >> " + inserts);
    }

    private Cursor getAllTrailers(String movieId) {
        String selection = TrailerEntry.COLUMN_MOVIES_FOREING_KEY+ " = ?";
        String[] selectionArgs = {
                movieId
        };

        Cursor cursor = database.query(MoviesContract.TrailerEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);
        Log.d("DETAILS", "AllTrailers: " + cursor.getCount());
        return cursor;
    }

    private Cursor getAllReviews(String movieId) {
        String selection = ReviewEntry.COLUMN_MOVIES_FOREING_KEY+ " = ?";
        String[] selectionArgs = {
                movieId
        };

        Cursor cursor = database.query(MoviesContract.ReviewEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null);

        return cursor;
    }
}
