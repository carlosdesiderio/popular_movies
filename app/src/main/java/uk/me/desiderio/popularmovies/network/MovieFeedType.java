package uk.me.desiderio.popularmovies.network;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Defines types of request to the '/movies' feed
 */

public class MovieFeedType {
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({POPULAR_MOVIES_FEED, TOP_RATED_MOVIES_FEED})
    public @interface FeedType {}
    public static final String  POPULAR_MOVIES_FEED = "popular";
    public static final String  TOP_RATED_MOVIES_FEED = "top_rated";
}
