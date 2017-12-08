package uk.me.desiderio.popularmovies.view;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import uk.me.desiderio.popularmovies.DetailsActivity;
import uk.me.desiderio.popularmovies.MainActivity;
import uk.me.desiderio.popularmovies.R;
import uk.me.desiderio.popularmovies.data.DataUtils;
import uk.me.desiderio.popularmovies.data.MoviesContract;
import uk.me.desiderio.popularmovies.network.ConnectivityUtils.ConnectivityState;

import static uk.me.desiderio.popularmovies.network.ConnectivityUtils.CONNECTED;
import static uk.me.desiderio.popularmovies.network.ConnectivityUtils.DISCONNECTED;
import static uk.me.desiderio.popularmovies.network.ConnectivityUtils.checkConnectivity;

/**
 * Util class for the {@link MainActivity} and the {@link DetailsActivity}
 */

public class ViewUtils {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FLAG_NO_VIEW_DATA, FLAG_STALE_VIEW_DATA, FLAG_FRESH_VIEW_DATA})
    private @interface ViewDataAvailability{}
    private static final int FLAG_NO_VIEW_DATA = 0;
    private static final int FLAG_STALE_VIEW_DATA = 1;
    private static final int FLAG_FRESH_VIEW_DATA = 2;

    /**
     * check whether the cursor provided as its parameters
     * has data and if that data is older that the time span
     * defined by the {@link MoviesContract}
     */
    @ViewDataAvailability
    private static int getViewDataAvailability(@NonNull Cursor cursor) {
        int isdataAvail = FLAG_NO_VIEW_DATA;
        if (cursor.getCount() > 0) {
            isdataAvail = FLAG_STALE_VIEW_DATA;
            if(!DataUtils.isDataStale(cursor, MoviesContract.STALE_DATA_MAX_LIFE_SPAN)) {
                isdataAvail = ViewUtils.FLAG_FRESH_VIEW_DATA;
            }
        }

        return isdataAvail;
    }

    /**
     * returns true when :
     * -  data is not is older that the time span defined by the {@link MoviesContract}
     * -  no connection to update data that is older that the time span defined
     *    by the {@link MoviesContract}
     */
    public static boolean hasAnyDataToShow(@NonNull Context context, @NonNull Cursor cursor) {
        int viewDataState = getViewDataAvailability(cursor);
        boolean isDisconnected = checkConnectivity(context) == DISCONNECTED;

        return viewDataState == FLAG_FRESH_VIEW_DATA ||
                (viewDataState == FLAG_STALE_VIEW_DATA && isDisconnected);
    }


    /**
     * returns instance of {@link Snackbar}
     * the bar is customised depending on the connectivity state provided as parameter
     */
    @NonNull
    public static Snackbar getSnackbar(@ConnectivityState int connectivityState, @NonNull View view, View.OnClickListener listener) {
        Context context = view.getContext();
        String message;
        int duration;

        switch (connectivityState) {
            case CONNECTED:
                message = context.getString(R.string.snackbar_connected_message);
                duration = Snackbar.LENGTH_INDEFINITE;
                break;
            case DISCONNECTED:
                message = context.getString(R.string.snackbar_no_connection_message);
                duration = Snackbar.LENGTH_SHORT;
                break;
            default:
                message = "";
                duration = Snackbar.LENGTH_INDEFINITE;
        }

        final Snackbar snackbar = Snackbar
                .make(view, message, duration);


        if (connectivityState == CONNECTED) {
            String buttonLabel = context.getString(R.string.snackbar_button_label);
            snackbar.setAction(buttonLabel, listener);
        }

        return snackbar;
    }
}
