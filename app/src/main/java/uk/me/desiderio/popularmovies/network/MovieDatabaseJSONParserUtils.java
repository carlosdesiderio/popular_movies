package uk.me.desiderio.popularmovies.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.me.desiderio.popularmovies.data.Movie;

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
            movies.add(movie);
        }

        return movies;
    }

}
