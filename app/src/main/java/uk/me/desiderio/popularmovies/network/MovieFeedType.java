package uk.me.desiderio.popularmovies.network;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import uk.me.desiderio.popularmovies.MainActivity;

/**
 * Defines feed types to be displayed in the {@link MainActivity}
 */

public class MovieFeedType {
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({POPULAR_MOVIES_FEED, TOP_RATED_MOVIES_FEED, FAVORITES_MOVIES_FEED})
    public @interface FeedType {}
    public static final String  POPULAR_MOVIES_FEED = "popular";
    public static final String  TOP_RATED_MOVIES_FEED = "top_rated";
    public static final String  FAVORITES_MOVIES_FEED = "favorites";
}
