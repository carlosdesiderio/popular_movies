package uk.me.desiderio.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

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

    public Movie(int id, String title, String date, String synopsis, double voteAverage, String posterURLString) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.synopsis = synopsis;
        this.voteAverage = voteAverage;
        this.posterURLString = posterURLString;
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
    @NonNull
    public String getPosterURLPathString() {
        return posterURLString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private Movie(@NonNull Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.date = in.readString();
        this.synopsis = in.readString();
        this.voteAverage = in.readDouble();
        this.posterURLString = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(date);
        parcel.writeString(synopsis);
        parcel.writeDouble(voteAverage);
        parcel.writeString(posterURLString);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(@NonNull Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
