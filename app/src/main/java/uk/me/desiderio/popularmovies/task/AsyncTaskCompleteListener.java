package uk.me.desiderio.popularmovies.task;

/**
 * Callback for {@link android.os.AsyncTask} completion event
 */

public interface AsyncTaskCompleteListener<T> {
    public void onTaskComplete(T result);
}
