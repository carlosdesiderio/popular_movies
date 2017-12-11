package uk.me.desiderio.popularmovies.task;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Service to load and parse data from the Movies Database service
 */

public class MoviesIntentService extends IntentService {

    private static final String SERVICE_NAME = MoviesIntentService.class.getSimpleName();

    public static final String EXTRA_FEED_TYPE = "extra_feed_type";
    public static final String EXTRA_MOVIE_ID = "extra_movie_id";

    public MoviesIntentService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        MoviesRequestTasks.executeTask(this, intent);
    }
}
