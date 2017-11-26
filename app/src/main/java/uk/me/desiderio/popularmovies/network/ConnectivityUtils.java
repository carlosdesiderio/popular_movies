package uk.me.desiderio.popularmovies.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by desiderio on 26/11/2017.
 */

public class ConnectivityUtils {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CONNECTED, DISCONNECTED})
    public @interface ConnectivityState {}
    public static final int CONNECTED = 1;
    public static final int DISCONNECTED = 0;


    @ConnectivityState
    public static int checkConnectivity(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();

        return (isConnected) ? CONNECTED : DISCONNECTED;
    }
}
