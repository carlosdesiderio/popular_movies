package uk.me.desiderio.popularmovies.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.data.MovieReview;
import uk.me.desiderio.popularmovies.data.MovieTrailer;

/**
 * Utility class to hold setting for the parsing of "The Movie Database" JSON response
 */

public class MovieDatabaseJSONParserUtils {

    private static final String NODE_NAME_ID = "id";
    private static final String NODE_NAME_RESULTS = "results";
    private static final String NODE_NAME_TITLE = "original_title";
    private static final String NODE_NAME_VOTE_AVERAGE = "vote_average";
    private static final String NODE_NAME_POSTER_PATH = "poster_path";
    private static final String NODE_NAME_RELEASE_DATE = "release_date";
    private static final String NODE_NAME_OVERVIEW = "overview";

    // trailer response node names
    private static final String NODE_NAME_NAME = "name";
    private static final String NODE_NAME_KEY = "key";
    private static final String NODE_NAME_SITE = "site";
    private static final String NODE_SITE_YOUTUBE_FLAG = "YouTube";

    // review response node names
    private static final String NODE_NAME_AUTHOR = "author";
    private static final String NODE_NAME_CONTENT = "content";
    private static final String NODE_NAME_URL = "url";
    /**
     * Parses Movies DB's JSON request response into a set of {@link Movie} objects
     */
    public static List<Movie> parseJsonString(String jsonString) throws JSONException {
        // holds all the result child nodes
        List<Movie> movies = new ArrayList<>();

        JSONObject moviesJson = new JSONObject(jsonString);

        JSONArray moviesArray = moviesJson.getJSONArray(NODE_NAME_RESULTS);

        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movieJSONObject = moviesArray.getJSONObject(i);

            int id = movieJSONObject.getInt(NODE_NAME_ID);
            String title = movieJSONObject.getString(NODE_NAME_TITLE);
            String date = movieJSONObject.getString(NODE_NAME_RELEASE_DATE);
            String synopsis = movieJSONObject.getString(NODE_NAME_OVERVIEW);
            double voteAverage = movieJSONObject.getDouble(NODE_NAME_VOTE_AVERAGE);
            String posterUrlPath = movieJSONObject.getString(NODE_NAME_POSTER_PATH);

            Movie movie = new Movie(id, title, date, synopsis, voteAverage, posterUrlPath);
            Log.d("PARSE", " >>>>>> " + MovieDatabaseRequestUtils.getMovieReviewsUrl(String.valueOf(id)).toString());
            movies.add(movie);
        }

        return movies;
    }

    public static List<MovieTrailer> parseTrailerJsonString(String jsonString) throws JSONException  {
        List<MovieTrailer> trailers = new ArrayList<>();

        JSONObject trailersJson = new JSONObject(jsonString);

        JSONArray trailersArray = trailersJson.getJSONArray(NODE_NAME_RESULTS);

        for (int i = 0; i < trailersArray.length(); i++) {
            JSONObject trailerJSONObject = trailersArray.getJSONObject(i);
            String site = trailerJSONObject.getString(NODE_NAME_SITE);
                Log.d("PARSER", "trailers >> checking if available at YouTube");

            if(site.equals(NODE_SITE_YOUTUBE_FLAG)) {
                String name = trailerJSONObject.getString(NODE_NAME_NAME);
                String id = trailerJSONObject.getString(NODE_NAME_ID);
                String key = trailerJSONObject.getString(NODE_NAME_KEY);

                MovieTrailer trailer = new MovieTrailer(id, name, key);

                trailers.add(trailer);

                Log.d("PARSER", "trailers added>> " + name);
            }

        }

        return trailers;
    }

    public static List<MovieReview> parseReviewsJsonString(String jsonString) throws JSONException  {
        List<MovieReview> reviews = new ArrayList<>();

        JSONObject reviewsJson = new JSONObject(jsonString);
        JSONArray reviewsArray = reviewsJson.getJSONArray(NODE_NAME_RESULTS);

        for (int i = 0; i < reviewsArray.length(); i++) {
            JSONObject reviewsJSONObject = reviewsArray.getJSONObject(i);
            String id = reviewsJSONObject.getString(NODE_NAME_ID);
            String author = reviewsJSONObject.getString(NODE_NAME_AUTHOR);
            String content = reviewsJSONObject.getString(NODE_NAME_CONTENT);
            String url = reviewsJSONObject.getString(NODE_NAME_URL);

            MovieReview review = new MovieReview(id, author,content, url);
            reviews.add(review);

            Log.d("PARSER", "review added by >> " + author);
        }
        return reviews;
    }
}
