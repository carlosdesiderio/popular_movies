package uk.me.desiderio.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import uk.me.desiderio.popularmovies.data.MoviesContract.FavoritessEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.MoviesEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.ReviewEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.TrailerEntry;

/**
 * Content provider to persist and access data from the movies.db
 */

public class MovieContentProvider extends ContentProvider {

    public static final String TAG = MovieContentProvider.class.getSimpleName();

    public static final int MOVIES = 100;
    public static final int MOVIES_WITH_ID = 101;
    public static final int TRAILERS = 200;
    public static final int TRAILERS_WITH_ID = 201;
    public static final int REVIEWS = 300;
    public static final int REVIEWS_WITH_ID = 301;
    public static final int FAVORITES = 400;

    public static final UriMatcher uriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // movies directory
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES, MOVIES);
        // single movie
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES + "/#", MOVIES_WITH_ID);
        // trailer directory
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_TRAILERS, TRAILERS);
        // single trailer
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_TRAILERS + "/#", TRAILERS_WITH_ID);
        // favorites directory
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_FAVORITES, FAVORITES);
        // single directory
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_FAVORITES + "/#", FAVORITES);
        // review directory
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_REVIEWS, REVIEWS);
        // single review
        uriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_REVIEWS + "/#", REVIEWS_WITH_ID);

        return uriMatcher;
    }

    private MovieDBHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new MovieDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();

        int match = uriMatcher.match(uri);

        String tableName;

        switch (match) {
            case MOVIES:
                tableName = MoviesEntry.TABLE_NAME;
                break;
            case TRAILERS:
                tableName = TrailerEntry.TABLE_NAME;
                break;
            case REVIEWS:
                tableName = ReviewEntry.TABLE_NAME;
                break;
            case FAVORITES:
                tableName = FavoritessEntry.TABLE_NAME;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }


        Cursor cursor = database.query(tableName,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        Log.d(TAG, "Returning " + cursor.getCount() + " data items from table " +tableName );

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = uriMatcher.match(uri);

        Uri returnUri;

        switch (match) {
            case MOVIES:
                returnUri = doInsert(MoviesEntry.TABLE_NAME, MoviesEntry.CONTENT_URI, contentValues);
            break;
           case MOVIES_WITH_ID:
               returnUri = null;
            break;
           case TRAILERS:
               returnUri = doInsert(TrailerEntry.TABLE_NAME, TrailerEntry.CONTENT_URI, contentValues);
            break;
           case TRAILERS_WITH_ID:
               returnUri = null;
            break;
           case REVIEWS:
               returnUri = doInsert(ReviewEntry.TABLE_NAME, ReviewEntry.CONTENT_URI, contentValues);
            break;
           case REVIEWS_WITH_ID:
               returnUri = null;
            break;
            case FAVORITES:
                returnUri = doInsert(FavoritessEntry.TABLE_NAME, FavoritessEntry.CONTENT_URI, contentValues);
                break;
           default:
               throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        int match = uriMatcher.match(uri);

        switch (match) {
            case MOVIES:
                return executedBulkInsertAt(MoviesEntry.TABLE_NAME, uri, values);
            case TRAILERS:
                return executedBulkInsertAt(TrailerEntry.TABLE_NAME, uri, values);
            case REVIEWS:
                return executedBulkInsertAt(ReviewEntry.TABLE_NAME, uri, values);

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = uriMatcher.match(uri);

        String directoryBasePath ="vnd.android.cursor.dir" + "/" + MoviesContract.CONTENT_AUTHORITY + "/";
        String itemBasePath = "vnd.android.cursor.item" + "/" + MoviesContract.CONTENT_AUTHORITY + "/";

        switch (match) {
            case MOVIES:
                return directoryBasePath + MoviesContract.PATH_MOVIES;
            case TRAILERS:
                return directoryBasePath + MoviesContract.PATH_TRAILERS;
            case REVIEWS:
                return directoryBasePath + MoviesContract.PATH_REVIEWS;
            case MOVIES_WITH_ID:
                return itemBasePath + MoviesContract.PATH_MOVIES;
            case TRAILERS_WITH_ID:
                return itemBasePath + MoviesContract.PATH_TRAILERS;
            case REVIEWS_WITH_ID:
                return itemBasePath + MoviesContract.PATH_REVIEWS;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private Uri doInsert(String tableName, Uri contentUri, ContentValues contentValues) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        Uri returnUri;

        long id = database.insert(tableName, null, contentValues);
        Log.d(TAG, "Data item inserted with id: " + id);
        if(id != -1) {
            returnUri = ContentUris.withAppendedId(contentUri, id);
        } else {
            throw new SQLException("Failed to insert raw in uri : " + contentUri);
        }
        return returnUri;
    }

    private int executedBulkInsertAt(@NonNull String tableName, @NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();
        Log.d(TAG, "Bulk inserting : " + values.length + " data items");

        int rowInserted = 0;

        database.beginTransaction();
        try {
            for (ContentValues value : values) {
                long id = database.insert(tableName, null, value);
                if (id != -1) {
                    rowInserted++;
                }
            }
            database.setTransactionSuccessful();

        } finally {
            database.endTransaction();
        }

        if (rowInserted > 0) {
            Log.d(TAG, "Bulk insertion completed  : " + values.length + " data items");
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowInserted;
    }
    
    // unsupported actions

    @Override
    public int delete(@NonNull Uri uri, @Nullable String whereClause, @Nullable String[] whereArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int match = uriMatcher.match(uri);

        int deletedRows;

        switch (match) {
            case FAVORITES:
                deletedRows = database.delete(FavoritessEntry.TABLE_NAME,
                        whereClause,
                        whereArgs);
                break;
            default:
                throw new UnsupportedOperationException("Delete is not a supported action from this provider. Unknown uri: " + uri);
        }

        if (deletedRows > 0) {
            Log.d(TAG, "Delition completed  : " + deletedRows + " rows deleted");
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        throw new UnsupportedOperationException("Update is not a supported action from this provider");
    }
}
