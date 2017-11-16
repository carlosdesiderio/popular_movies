package uk.me.desiderio.popularmovies.task;

import android.os.AsyncTask;

import java.net.URL;
import java.util.List;

import uk.me.desiderio.popularmovies.data.MovieTrailer;
import uk.me.desiderio.popularmovies.network.MovieDatabaseJSONParserUtils;
import uk.me.desiderio.popularmovies.network.MovieDatabaseRequestUtils;

/**
 * Task to request movie data asynchronously
 */

public class MovieTrailersRequestAsyncTask extends AsyncTask<String, Void, List<MovieTrailer>> {

    private AsyncTaskCompleteListener listener;

    public MovieTrailersRequestAsyncTask(AsyncTaskCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<MovieTrailer> doInBackground(String... movieIds) {
        URL url = MovieDatabaseRequestUtils.getMovieTrailersUrl(movieIds[0]);
        try {
            // requests data from movie db service
            String response = MovieDatabaseRequestUtils.getResponseFromHttpUrl(url);
            // parses json data into a list of Movie objects
            return MovieDatabaseJSONParserUtils.parseTrailerJsonString(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<MovieTrailer> trailers) {
        listener.onTrailerTaskComplete(trailers);
    }

    public interface AsyncTaskCompleteListener {
        void onTrailerTaskComplete(List<MovieTrailer> result);
    }
}
