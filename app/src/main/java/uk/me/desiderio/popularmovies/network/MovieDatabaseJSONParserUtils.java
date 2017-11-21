package uk.me.desiderio.popularmovies.network;

import android.content.ContentValues;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.data.MoviesContract;

/**
 * Utility class to hold setting for the parsing of "The Movie Database" JSON response
 */

public class MovieDatabaseJSONParserUtils {

    private static final String TAG = MovieDatabaseJSONParserUtils.class.getSimpleName();

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
    public static ContentValues[]  parseMovieJsonString(String jsonString) throws JSONException {
        JSONObject moviesJson = new JSONObject(jsonString);

        JSONArray moviesArray = moviesJson.getJSONArray(NODE_NAME_RESULTS);

        ContentValues[] valuesArray = new ContentValues[moviesArray.length()];
        for (int i = 0; i < moviesArray.length(); i++) {
            ContentValues values = new ContentValues();

            JSONObject movieJSONObject = moviesArray.getJSONObject(i);

            int id = movieJSONObject.getInt(NODE_NAME_ID);
            String title = movieJSONObject.getString(NODE_NAME_TITLE);
            String date = movieJSONObject.getString(NODE_NAME_RELEASE_DATE);
            String synopsis = movieJSONObject.getString(NODE_NAME_OVERVIEW);
            double voteAverage = movieJSONObject.getDouble(NODE_NAME_VOTE_AVERAGE);
            String posterUrlPath = movieJSONObject.getString(NODE_NAME_POSTER_PATH);

            values.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, id);
            values.put(MoviesContract.MoviesEntry.COLUMN_TITLE, title);
            values.put(MoviesContract.MoviesEntry.COLUMN_DATE, date);
            values.put(MoviesContract.MoviesEntry.COLUMN_SYNOPSIS, synopsis);
            values.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, voteAverage);
            values.put(MoviesContract.MoviesEntry.COLUMN_POSTER_URL, posterUrlPath);

            valuesArray[i] = values;

            Log.d(TAG, "Movie parsed: >> " +  title);
        }

        return valuesArray;
    }

    public static ContentValues[] parseTrailerJsonString(String jsonString) throws JSONException  {
        JSONObject trailersJson = new JSONObject(jsonString);

        JSONArray trailersArray = trailersJson.getJSONArray(NODE_NAME_RESULTS);

        ContentValues[] valuesList = new ContentValues[trailersArray.length()];
        for (int i = 0; i < trailersArray.length(); i++) {
            JSONObject trailerJSONObject = trailersArray.getJSONObject(i);
            String site = trailerJSONObject.getString(NODE_NAME_SITE);
            Log.d("PARSER", "trailers >> checking if available at YouTube");

            // only stores youtube trailers
            if(site.equals(NODE_SITE_YOUTUBE_FLAG)) {
                ContentValues value = new ContentValues();

                String name = trailerJSONObject.getString(NODE_NAME_NAME);
                String id = trailerJSONObject.getString(NODE_NAME_ID);
                String key = trailerJSONObject.getString(NODE_NAME_KEY);

                value.put(MoviesContract.TrailerEntry.COLUMN_TRAILER_ID, id);
                value.put(MoviesContract.TrailerEntry.COLUMN_NAME, name);
                value.put(MoviesContract.TrailerEntry.COLUMN_KEY, key);

                valuesList[i] = value;

                Log.d(TAG, "Trailer parsed: >> " +  name);
            }
        }
        return valuesList;
    }

    public static ContentValues[] parseReviewsJsonString(String jsonString) throws JSONException  {
        JSONObject reviewsJson = new JSONObject(jsonString);
        JSONArray reviewsArray = reviewsJson.getJSONArray(NODE_NAME_RESULTS);

        ContentValues[] valuesList = new ContentValues[reviewsArray.length()];
        for (int i = 0; i < reviewsArray.length(); i++) {
            ContentValues values = new ContentValues();

            JSONObject reviewsJSONObject = reviewsArray.getJSONObject(i);
            String id = reviewsJSONObject.getString(NODE_NAME_ID);
            String author = reviewsJSONObject.getString(NODE_NAME_AUTHOR);
            String content = reviewsJSONObject.getString(NODE_NAME_CONTENT);
            String url = reviewsJSONObject.getString(NODE_NAME_URL);

            values.put(MoviesContract.ReviewEntry.COLUMN_REVIEW_ID, id);
            values.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, author);
            values.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, content);
            values.put(MoviesContract.ReviewEntry.COLUMN_URL, url);

            valuesList[i] = values;

            Log.d(TAG, "Review parsed: >> " + author);
        }
        return valuesList;
    }
}
