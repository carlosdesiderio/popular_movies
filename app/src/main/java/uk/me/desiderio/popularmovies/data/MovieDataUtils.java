package uk.me.desiderio.popularmovies.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides mock movie data
 */

public class MovieDataUtils {


    public static List<Movie> getMovieMockData() {
        ArrayList<Movie> movies = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            movies.add(new Movie("Scary Movie " + i,
                    "23/3/45",
                    120,
                    "This a very scary movie where girl meets boy",
                    8,
                    "url",
                    null));

        }

        return movies;
    }
}
