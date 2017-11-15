package uk.me.desiderio.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Data object class to hold info about a movie instance
 */

public class Movie implements Parcelable {

    private final int id;
    private final String title;
    private final String date;
    private final String synopsis;
    private final double voteAverage;
    private final String posterURLString;
    private final List<MovieTrailer> trailers;
    private final List<MovieReview> reviews;

    public Movie(int id, String title, String date, String synopsis, double voteAverage, String posterURLString) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.synopsis = synopsis;
        this.voteAverage = voteAverage;
        this.posterURLString = posterURLString;

        this.trailers = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    /**
     * add movie trailer
     */
    public void addTrailer(MovieTrailer trailer) {
        trailers.add(trailer);
    }

    /**
     * add movie review
     */
    public void addReview(MovieReview review) {
        reviews.add(review);
    }

    /**
     * returns movie's database unique id in minutes as a integer
     */
    public int getId() {
        return id;
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
     * returns movie's plot synopsis as a String
     */
    public String getSynopsis() {
        return synopsis;
    }

    /**
     * returns movie's average vote as an integer
     */
    public double getVoteAverage() {
        return voteAverage;
    }

    /**
     * returns movie's poster url as a string
     */
    public String getPosterURLPathString() {
        return posterURLString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Movie(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.date = in.readString();
        this.synopsis = in.readString();
        this.voteAverage = in.readDouble();
        this.posterURLString = in.readString();
        if (in.readByte() == 0x01) {
            trailers = new ArrayList<>();
            in.readList(trailers, MovieTrailer.class.getClassLoader());
        } else {
            trailers = null;
        }
        if (in.readByte() == 0x01) {
            reviews = new ArrayList<>();
            in.readList(reviews, MovieReview.class.getClassLoader());
        } else {
            reviews = null;
        }

    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(date);
        parcel.writeString(synopsis);
        parcel.writeDouble(voteAverage);
        parcel.writeString(posterURLString);
        if (trailers == null) {
            parcel.writeByte((byte) (0x00));
        } else {
            parcel.writeByte((byte) (0x01));
            parcel.writeList(trailers);
        }
        if (reviews == null) {
            parcel.writeByte((byte) (0x00));
        } else {
            parcel.writeByte((byte) (0x01));
            parcel.writeList(reviews);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
