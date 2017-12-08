package uk.me.desiderio.popularmovies.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Utility class to check if the device has an available connection
 */

public class ConnectivityUtils {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CONNECTED, DISCONNECTED})
    public @interface ConnectivityState {}
    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 0;

    @ConnectivityState
    public static int checkConnectivity(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if(cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();

        return (isConnected) ? CONNECTED : DISCONNECTED;
    }
}
