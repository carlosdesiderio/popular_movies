package uk.me.desiderio.popularmovies.task;

import android.os.AsyncTask;

import java.net.URL;
import java.util.List;

import uk.me.desiderio.popularmovies.data.MovieReview;
import uk.me.desiderio.popularmovies.network.MovieDatabaseJSONParserUtils;
import uk.me.desiderio.popularmovies.network.MovieDatabaseRequestUtils;

/**
 * Task to request movie reviews data asynchronously
 */

public class MovieReviewsRequestAsyncTask extends AsyncTask<String, Void, List<MovieReview>> {

    private AsyncTaskCompleteListener listener;

    public MovieReviewsRequestAsyncTask(AsyncTaskCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    protected List<MovieReview> doInBackground(String... movieIds) {
        URL url = MovieDatabaseRequestUtils.getMovieReviewsUrl(movieIds[0]);
        try {
            // requests data from movie db service
            String response = MovieDatabaseRequestUtils.getResponseFromHttpUrl(url);
            // parses json data into a list of Movie objects
            return MovieDatabaseJSONParserUtils.parseReviewsJsonString(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<MovieReview> reviews) {
        listener.onReviewTaskComplete(reviews);
    }

    public interface AsyncTaskCompleteListener {
        void onReviewTaskComplete(List<MovieReview> result);
    }
}
