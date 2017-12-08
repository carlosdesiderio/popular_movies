package uk.me.desiderio.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Data class for the movie's trailer
 */

@SuppressWarnings("unused")
public class MovieTrailer implements Parcelable{

    private final String id;
    // key to access the trailer in YouTube
    private final String name;
    private final String key;

    public MovieTrailer(String id, String name, String key) {
        this.id = id;
        this.name = name;
        this.key = key;
    }

    private MovieTrailer(@NonNull Parcel in) {
        id = in.readString();
        name = in.readString();
        key = in.readString();
    }

    public String getId() {
        return id;
    }

    /**
     * returns identifier to play video in YouTube
     */
    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(key);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MovieTrailer> CREATOR
            = new Parcelable.Creator<MovieTrailer>() {
        public MovieTrailer createFromParcel(@NonNull Parcel in) {
            return new MovieTrailer(in);
        }

        public MovieTrailer[] newArray(int size) {
            return new MovieTrailer[size];
        }
    };
}
