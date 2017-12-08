package uk.me.desiderio.popularmovies.task;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URL;

import uk.me.desiderio.popularmovies.data.MoviesContract;
import uk.me.desiderio.popularmovies.network.MovieDatabaseJSONParserUtils;
import uk.me.desiderio.popularmovies.network.MovieDatabaseRequestUtils;
import uk.me.desiderio.popularmovies.network.MovieFeedType;
import uk.me.desiderio.popularmovies.network.MovieFeedType.FeedType;

/**
 * Provides task to be run in the background by the {@link MoviesIntentService}
 */

public class MoviesRequestTasks {

    private static final String TAG = MoviesRequestTasks.class.getSimpleName();

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ACTION_REQUEST_MOVIE_DATA,
            ACTION_REQUEST_TRAILER_DATA,
            ACTION_REQUEST_REVIEW_DATA})
    private @interface ActionName{}

    public static final String ACTION_REQUEST_MOVIE_DATA = "request_movie_data";
    public static final String ACTION_REQUEST_TRAILER_DATA = "request_trailer_data";
    public static final String ACTION_REQUEST_REVIEW_DATA = "request_review_data";

    static void executeTask(@NonNull Context context, @Nullable Intent intent) {
        if(intent == null) {
            return;
        }

        @ActionName
        String action = intent.getAction();


        if (action != null) {
            switch (action) {
                case ACTION_REQUEST_MOVIE_DATA:
                    String feedType = intent.getStringExtra(MoviesIntentService.EXTRA_FEED_TYPE);
                    ContentValues[] moviesList = requestMovieData(feedType);
                    bulkInsertMoviesInDatabase(context, moviesList, feedType);
                    break;
                case ACTION_REQUEST_TRAILER_DATA: {
                    String movieId = intent.getStringExtra(MoviesIntentService.EXTRA_MOVIE_ID);
                    ContentValues[] trailers = requestTrailerData(movieId);
                    bulkInsertTrailersInDatabase(context, trailers, movieId);
                    break;
                }
                case ACTION_REQUEST_REVIEW_DATA: {
                    String movieId = intent.getStringExtra(MoviesIntentService.EXTRA_MOVIE_ID);
                    ContentValues[] reviews = requestReviewData(movieId);
                    bulkInsertReviewsInDatabase(context, reviews, movieId);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknoww action: " + action);
            }
        }
    }

    private static ContentValues[] requestMovieData(@NonNull @FeedType String feedType) {

        String responseString;
        try {
            // requests data from movie db service
            URL url = getMoviesRequestUrl(feedType);
            responseString = MovieDatabaseRequestUtils.getResponseFromHttpUrl(url);
            // parses json data into a list of Movie objects
            return MovieDatabaseJSONParserUtils.parseMovieJsonString(responseString);
        } catch (@NonNull IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static ContentValues[] requestTrailerData(@NonNull String movieId) {
        URL url = MovieDatabaseRequestUtils.getMovieTrailersUrl(movieId);
        if (url != null) {
            try {
                // requests data from movie db service
                String response = MovieDatabaseRequestUtils.getResponseFromHttpUrl(url);
                // parses json data into a list of Movie objects
                return MovieDatabaseJSONParserUtils.parseTrailerJsonString(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static ContentValues[] requestReviewData(@NonNull String movieId) {
        URL url = MovieDatabaseRequestUtils.getMovieReviewsUrl(movieId);
        if (url != null) {
            try {
                // requests data from movie db service
                String response = MovieDatabaseRequestUtils.getResponseFromHttpUrl(url);
                // parses json data into a list of Movie objects
                return MovieDatabaseJSONParserUtils.parseReviewsJsonString(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private static void bulkInsertMoviesInDatabase(@NonNull Context context, @Nullable ContentValues[] movies, @NonNull String feedType) {
        if (movies != null && movies.length > 0) {
            ContentValues[] contentValues = putValueInto(movies,
                    MoviesContract.MoviesEntry.COLUMN_FEED_TYPE,
                    feedType);
            int rowsInserted = context.getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, contentValues);

            Log.d(TAG, "Movie Bulk Insert :: Rows inserted: " + rowsInserted + " with feed type: " + feedType);
        } else {
            Log.d(TAG, "Movie Bulk Insert :: NO Rows inserted. with feed type: " + feedType);
        }
    }

    private static void bulkInsertTrailersInDatabase(@NonNull Context context, @Nullable ContentValues[] trailers, @NonNull String movieId) {
        if (trailers != null && trailers.length > 0) {
            ContentValues[] contentValues = putValueInto(trailers,
                    MoviesContract.TrailerEntry.COLUMN_MOVIES_FOREING_KEY,
                    movieId);
            int rowsInserted = context.getContentResolver().bulkInsert(MoviesContract.TrailerEntry.CONTENT_URI, contentValues);

            Log.d(TAG, "Trailer Bulk Insert :: Rows inserted: " + rowsInserted + " for movie " + movieId);
        } else {
            Log.d(TAG, "Trailer Bulk Insert :: NO Rows inserted for movie " + movieId);
        }
    }

    private static void bulkInsertReviewsInDatabase(@NonNull Context context, @Nullable ContentValues[] reviews, @NonNull String movieId) {
        if (reviews != null && reviews.length > 0) {
            ContentValues[] contentValues = putValueInto(reviews,
                    MoviesContract.ReviewEntry.COLUMN_MOVIES_FOREING_KEY,
                    movieId);
            int rowsInserted = context.getContentResolver().bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, contentValues);

            Log.d(TAG, "Review Bulk Insert :: Rows inserted: " + rowsInserted + " for movie " + movieId);
        } else {
            Log.d(TAG, "Review Bulk Insert :: NO Rows inserted for movie " + movieId);
        }
    }


    private static URL getMoviesRequestUrl(@NonNull @FeedType String feedType) {
        switch (feedType) {
            case MovieFeedType.POPULAR_MOVIES_FEED:
                return MovieDatabaseRequestUtils.getPopularMoviesUrl();
            case MovieFeedType.TOP_RATED_MOVIES_FEED:
                return MovieDatabaseRequestUtils.getTopRatedMoviesUrl();
            default:
                throw new IllegalArgumentException("Unknoww parameter: " + feedType);
        }
    }

    @NonNull
    private static ContentValues[] putValueInto(@NonNull ContentValues[] valuesArray, String key, String value) {
        for (ContentValues aValuesArray : valuesArray) {
            aValuesArray.put(key, value);
        }
        return valuesArray;
    }
}
