package uk.me.desiderio.popularmovies.view;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

import uk.me.desiderio.popularmovies.R;
import uk.me.desiderio.popularmovies.network.ConnectivityUtils;
import uk.me.desiderio.popularmovies.network.ConnectivityUtils.ConnectivityState;

import static uk.me.desiderio.popularmovies.network.ConnectivityUtils.CONNECTED;
import static uk.me.desiderio.popularmovies.network.ConnectivityUtils.DISCONNECTED;

/**
 * Factory class for the {@link Snackbar}
 */

public class ViewFactory {

    public static Snackbar getSnackbar(@ConnectivityState int connectivityState, View view, View.OnClickListener listener) {
        Context context = view.getContext();
        String message;

        switch (connectivityState) {
            case CONNECTED:
                message = context.getString(R.string.snackbar_connected_message);
                break;
            case DISCONNECTED:
                message = context.getString(R.string.snackbar_no_connection_message);
                break;
            default:
                message = "";
        }

        final Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_INDEFINITE);


        if (connectivityState == CONNECTED) {
            String buttonLabel = context.getString(R.string.snackbar_button_label);
            snackbar.setAction(buttonLabel, listener);
        }

        return snackbar;
    }
}
