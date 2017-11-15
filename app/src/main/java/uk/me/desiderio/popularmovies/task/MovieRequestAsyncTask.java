package uk.me.desiderio.popularmovies.task;

import android.os.AsyncTask;

import java.net.URL;
import java.util.List;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.network.MovieDatabaseJSONParserUtils;
import uk.me.desiderio.popularmovies.network.MovieDatabaseRequestUtils;

/**
 * Created by desiderio on 15/11/2017.
 */

public class MovieRequestAsyncTask extends AsyncTask<URL, Void, List<Movie>> {

    AsyncTaskCompleteListener<List<Movie>> listener;

    public MovieRequestAsyncTask(AsyncTaskCompleteListener<List<Movie>> listener) {
        this.listener = listener;
    }

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
        listener.onTaskComplete(movies);
    }
}
