package uk.me.desiderio.popularmovies.view;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Comparator;

import uk.me.desiderio.popularmovies.DetailsActivity;

/**
 * Helper class to allow the {@link DetailsActivity} list to show different types of views depending
 * on the data to show.
 *
 * Holds the view type and original data list index as properties. {@link DetailsActivity} will hold
 * two different list for the data related to the trailers and reviews
 *
 * Provides a comparator so that the list view show the trailers first and the reviews below them.
 *
 */
public class DetailListViewItem{

    /** identifies view types for the recycling view.
     *  provides priority order in the list */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TRAILER_HEADING_ITEM_TYPE, TRAILER_ITEM_TYPE, REVIEW_HEADING_ITEM_TYPE, REVIEW_ITEM_TYPE})
    public @interface ViewType { }
    public static final int TRAILER_HEADING_ITEM_TYPE = 1;
    public static final int TRAILER_ITEM_TYPE = 2;
    public static final int REVIEW_HEADING_ITEM_TYPE = 3;
    public static final int REVIEW_ITEM_TYPE = 4;


    private final int originalIndex;
    private final int type;

    public DetailListViewItem(@ViewType int type, int originalIndex) {
        this.type = type;
        this.originalIndex = originalIndex;
    }

    public int getOriginalIndex() {
        return originalIndex;
    }

    public int getType() {
        return type;
    }

    public static class ViewItemComparator implements Comparator<DetailListViewItem> {

        @Override
        public int compare(@NonNull DetailListViewItem v1, @NonNull DetailListViewItem v2) {
            if(v1.type < v2.getType()) {
                return -1;
            } else if(v1.type > v2.getType()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}

