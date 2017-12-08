package uk.me.desiderio.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

import java.util.concurrent.TimeUnit;

/**
 * Contract for the movies database
 */
public class MoviesContract {

    public static final long STALE_DATA_MAX_LIFE_SPAN = TimeUnit.HOURS.toMillis(1);

    static final String CONTENT_AUTHORITY = "uk.me.desiderio.popularmovies";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    static final String PATH_MOVIES = "movies";
    static final String PATH_TRAILERS = "trailers";
    static final String PATH_REVIEWS = "reviews";
    static final String PATH_FAVORITES = "favorite";

    public interface TimedEntry extends BaseColumns {
        String _TIME_UPDATED = "_time";
    }

    public static final class MoviesEntry implements TimedEntry {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.
                buildUpon().
                appendPath(PATH_MOVIES).
                build();

        static final String TABLE_NAME = "movies";

        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_POSTER_URL = "poster_url_path";
        public static final String COLUMN_FEED_TYPE = "feed";
    }

    public static final class TrailerEntry implements TimedEntry {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.
                buildUpon().
                appendPath(PATH_TRAILERS).
                build();

        static final String TABLE_NAME = "trailers";

        public static final String COLUMN_TRAILER_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_MOVIES_FOREING_KEY = "movie_id";

    }

    public static final class ReviewEntry implements TimedEntry {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.
                buildUpon().
                appendPath(PATH_REVIEWS).
                build();

        static final String TABLE_NAME = "reviews";

        public static final String COLUMN_REVIEW_ID = "id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_MOVIES_FOREING_KEY = "movie_id";
    }

    public static final class FavoritessEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.
                buildUpon().
                appendPath(PATH_FAVORITES).
                build();

        static final String TABLE_NAME = "favorites";

        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_POSTER_URL = "poster_url_path";
    }
}
