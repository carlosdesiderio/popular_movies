package uk.me.desiderio.popularmovies.network;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import uk.me.desiderio.popularmovies.BuildConfig;

/**
 * Utility class holding 'The Movie Database' request settings
 */

public class MovieDatabaseRequestUtils {

    private static final String TAG = MovieDatabaseRequestUtils.class.getSimpleName();

    private static final String BASE_URL_STRING = "http://api.themoviedb.org/3/movie";
    private static final String POPULAR_MOVIES_URL_PATH = "popular";
    private static final String TOP_RATED_MOVIES_URL_PATH = "top_rated";
    private static final String API_KEY_QUERY_NAME = "api_key";
    private static final String API_KEY = BuildConfig.MOVIE_DB_API_TOKEN;

    private static final String IMAGE_BASE_URL_STRING = "http://image.tmdb.org/t/p/";
    private static final String IMAGE_SIZE_185_PATH = "w185";

    // movie id to be provided as argument
    private static final String VIDEO_URL_PATH_STRING = "%1$s/videos";
    private static final String REVIEWS_URL_PATH_STRING = "%1$s/reviews";

    /**
     * Provides Movies DB endpoint to retrieve list of trailer of the movie identify by its argument
     */
    @Nullable
    public static URL getMovieTrailersUrl(String movieId) {
        String videoUrlPath = String.format(VIDEO_URL_PATH_STRING, movieId);
        Uri uri = Uri.parse(BASE_URL_STRING).buildUpon()
                .appendEncodedPath(videoUrlPath)
                .appendQueryParameter(API_KEY_QUERY_NAME, API_KEY)
                .build();

        return getUrlFromURI(uri);
    }

    /**
     * Provides Movies DB endpoint to retrieve reviews about the movie identify by its argument
     */
    @Nullable
    public static URL getMovieReviewsUrl(String movieId) {
        String reviewsUrlPath = String.format(REVIEWS_URL_PATH_STRING, movieId);
        Uri uri = Uri.parse(BASE_URL_STRING).buildUpon()
                .appendEncodedPath(reviewsUrlPath)
                .appendQueryParameter(API_KEY_QUERY_NAME, API_KEY)
                .build();

        Log.d(TAG, "Reviews URI: " + uri.toString());
        return getUrlFromURI(uri);
    }

    /**
     * Provides Movies DB endpoint to request the Poster Image of the movie identify by argument provided
     */
    public static Uri getMoviePosterUri(String imageNamePathString) {
        Uri uri = Uri.parse(IMAGE_BASE_URL_STRING).buildUpon()
                .appendEncodedPath(IMAGE_SIZE_185_PATH)
                .appendEncodedPath(imageNamePathString)
                .build();

        Log.d(TAG, "Built Image URI: " + uri.toString());
        return uri;
    }

    /**
     * Provides Movies DB endpoint to retrieve list of Popular Movies
     */
    @Nullable
    public static URL getPopularMoviesUrl() {
        Uri uri = Uri.parse(BASE_URL_STRING).buildUpon()
                .appendPath(POPULAR_MOVIES_URL_PATH)
                .appendQueryParameter(API_KEY_QUERY_NAME, API_KEY)
                .build();

        Log.d(TAG, "Popular movies URI: " + uri.toString());
        return getUrlFromURI(uri);
    }

    /**
     * Provides Movies DB endpoint to retrieve list of Top Rated Movies
     */
    @Nullable
    public static URL getTopRatedMoviesUrl() {
        Uri uri = Uri.parse(BASE_URL_STRING).buildUpon()
                .appendPath(TOP_RATED_MOVIES_URL_PATH)
                .appendQueryParameter(API_KEY_QUERY_NAME, API_KEY)
                .build();

        return getUrlFromURI(uri);
    }

    @Nullable
    private static URL getUrlFromURI(@NonNull Uri uri) {
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Built URL: " + url);

        return url;
    }

    /**
     * Returns HTTP response as a String.
     */
    public static String getResponseFromHttpUrl(@NonNull URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
