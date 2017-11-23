package uk.me.desiderio.popularmovies.view;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Comparator;

import uk.me.desiderio.popularmovies.DetailsActivity;

/**
 * Helper class to allow the {@link DetailsActivity} list to show different types of view depending
 * on the data to show.
 *
 * Holds view type and original index of the data structure related to this type.
 *
 * Provides a comparator so that the list view show the trailers first and the reviews below them.
 *
 */
public class DetailListViewItem{

    @Retention(RetentionPolicy.SOURCE)
@IntDef({TRAILER_HEADING_ITEM_TYPE, TRAILER_ITEM_TYPE, REVIEW_HEADING_ITEM_TYPE, REVIEW_ITEM_TYPE})
public @interface ViewType { }

    // identifies view types for the recycling view.
    // provides priority order in the list
    public static final int TRAILER_HEADING_ITEM_TYPE = 1;
    public static final int TRAILER_ITEM_TYPE = 2;
    public static final int REVIEW_HEADING_ITEM_TYPE = 3;
    public static final int REVIEW_ITEM_TYPE = 4;


    private int originalIndex;
    private int type;

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
        public int compare(DetailListViewItem v1, DetailListViewItem v2) {
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

