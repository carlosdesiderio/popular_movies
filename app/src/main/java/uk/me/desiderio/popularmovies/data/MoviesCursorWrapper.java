package uk.me.desiderio.popularmovies.data;

import android.database.Cursor;
import android.database.CursorWrapper;

import uk.me.desiderio.popularmovies.MainActivity;
import uk.me.desiderio.popularmovies.network.MovieFeedType.FeedType;

/**
 * Cursor Wrapper for the Loader at the {@link MainActivity}
 *
 * The wrapper will allow the Loader to handle two types of request ( popular and top rated movies )
 * determined by the feed type property
 *
 * The class will persist the feed type through the loading processed so that it is present
 * at the callback method
 */

public class MoviesCursorWrapper extends CursorWrapper {

    private String feedType;

    public MoviesCursorWrapper(Cursor cursor, @FeedType String feedType) {
        super(cursor);
        this.feedType = feedType;
    }

    @FeedType
    public String getFeedType() {
        return feedType;
    }

}
