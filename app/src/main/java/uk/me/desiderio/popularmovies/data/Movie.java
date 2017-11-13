package uk.me.desiderio.popularmovies.data;

import java.io.Serializable;
import java.util.List;

/**
 * Data object class to hold data about a movie instance
 */

public class Movie implements Serializable {


    private static final long serialVersionUID = -7060210544600464481L;

    private final String title;
    private final String date;
    private final int duration;
    private final String synopsis;
    private final int voteAverage;
    private final String posterURLString;
    private final List<String> trailers;

    public Movie(String title, String date, int duration, String synopsis, int voteAverage, String posterURLString, List<String> trailers) {
        this.title = title;
        this.date = date;
        this.duration = duration;
        this.synopsis = synopsis;
        this.voteAverage = voteAverage;
        this.posterURLString = posterURLString;
        this.trailers = trailers;
    }

    /**
     * returns movie's title as a String
     */
    public String getTitle() {
        return title;
    }

    /**
     * returns movie's year of production
     */
    public String getDate() {
        return date;
    }

    /**
     * returns movie's duration in minutes as a string
     */
    public int getDuration() {
        return duration;
    }

    /**
     * returns movie's plot synopsis as a String
     */
    public String getSynopsis() {
        return synopsis;
    }

    /**
     * returns movie's average vote as an integer
     */
    public int getVoteAverage() {
        return voteAverage;
    }

    /**
     * returns movie's poster url as a string
     */
    public String getPosterURLString() {
        return posterURLString;
    }

    /**
     * returns movie's trailer urls as list of strings
     */
    public List<String> getTrailers() {
        return trailers;
    }

}
