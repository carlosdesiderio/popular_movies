package uk.me.desiderio.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import uk.me.desiderio.popularmovies.data.MoviesContract.FavoritessEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.MoviesEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.ReviewEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.TrailerEntry;

/**
 * Manages local database for Movies
 */

public class MovieDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 21;

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME + " (" +
                MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MoviesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULl, " +
                MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                MoviesEntry.COLUMN_POSTER_URL + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_FEED_TYPE + " TEXT NOT NULL, " +
                MoviesEntry._TIME_UPDATED + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , " +
                " UNIQUE (" + MoviesEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";
        final String SQL_MOVIES_UPDATE_TIMESTAMP_TRIGGER = getUpdateTimeStampTrigger(MoviesEntry.TABLE_NAME,
                MoviesEntry._TIME_UPDATED,
                MoviesEntry._ID);

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + "(" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TrailerEntry.COLUMN_TRAILER_ID + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_KEY + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_MOVIES_FOREING_KEY + " INTEGER NOT NULL, " +
                TrailerEntry._TIME_UPDATED + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + TrailerEntry.COLUMN_MOVIES_FOREING_KEY + ") REFERENCES " + MoviesEntry.TABLE_NAME + " (" + MoviesEntry.COLUMN_MOVIE_ID + ")" +
                "ON UPDATE CASCADE ON DELETE CASCADE" +
                " UNIQUE (" + TrailerEntry.COLUMN_TRAILER_ID + ") ON CONFLICT REPLACE);";
        final String SQL_TRAILER_UPDATE_TIMESTAMP_TRIGGER = getUpdateTimeStampTrigger(TrailerEntry.TABLE_NAME,
                TrailerEntry._TIME_UPDATED,
                TrailerEntry._ID);

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ReviewEntry.COLUMN_REVIEW_ID + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_URL + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_MOVIES_FOREING_KEY + " INTEGER NOT NULL, " +
                ReviewEntry._TIME_UPDATED + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (" + ReviewEntry.COLUMN_MOVIES_FOREING_KEY + ") REFERENCES " + MoviesEntry.TABLE_NAME + " (" + MoviesEntry.COLUMN_MOVIE_ID + ")" +
                "ON UPDATE CASCADE ON DELETE CASCADE" +
                " UNIQUE (" + ReviewEntry.COLUMN_REVIEW_ID + ") ON CONFLICT REPLACE);";
        final String SQL_REVIEW_UPDATE_TIMESTAMP_TRIGGER = getUpdateTimeStampTrigger(ReviewEntry.TABLE_NAME,
                ReviewEntry._TIME_UPDATED,
                ReviewEntry._ID);

        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoritessEntry.TABLE_NAME + " (" +
                FavoritessEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FavoritessEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULl, " +
                FavoritessEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                FavoritessEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                FavoritessEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                FavoritessEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                FavoritessEntry.COLUMN_POSTER_URL + " TEXT NOT NULL, " +
                " UNIQUE (" + MoviesEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";


        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);

        sqLiteDatabase.execSQL(SQL_MOVIES_UPDATE_TIMESTAMP_TRIGGER);
        sqLiteDatabase.execSQL(SQL_TRAILER_UPDATE_TIMESTAMP_TRIGGER);
        sqLiteDatabase.execSQL(SQL_REVIEW_UPDATE_TIMESTAMP_TRIGGER);


    }

    private static String getUpdateTimeStampTrigger(String tableName, String lastUpdateColumnName, String idColumnName) {
        String triggerName = String.format("update_%s_timestamp_trigger", tableName);
        return  "CREATE TRIGGER " + triggerName +
                "AFTER INSERT ON " + tableName + " FOR EACH ROW" +
                " BEGIN " +
                "  UPDATE " + tableName +
                "    SET " + lastUpdateColumnName + " = current_timestamp" +
                "    WHERE " + idColumnName + " = NEW." + idColumnName + ";" +
                " END";
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoritessEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }
}
