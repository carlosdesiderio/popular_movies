package uk.me.desiderio.popularmovies.data;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility methods for the movies data
 */

public class DataUtils {

    /**
     * determines if data provided as its first parameter is older that the maximum data lifespan
     * defined by the the second parameter
     */
    public static boolean isDataStale(@NonNull Cursor cursor, @SuppressWarnings("SameParameterValue") long maxDataLifeSpan) {
        cursor.moveToFirst();
        String timestamp = cursor.getString(cursor.getColumnIndex(MoviesContract.MoviesEntry._TIME_UPDATED));
        SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss", Locale.UK );
        try {
            Date date = format.parse(timestamp);
            long dataTimeMillis = date.getTime();
            long currentTimeMillis = System.currentTimeMillis();
            long dataLifeTimeMillis = currentTimeMillis - dataTimeMillis;
            Log.d("DataUtils", "Is data stale? data age : " + maxDataLifeSpan + " : " + dataLifeTimeMillis);

            if( dataLifeTimeMillis > maxDataLifeSpan) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }
}
