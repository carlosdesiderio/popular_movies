package uk.me.desiderio.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by desiderio on 15/11/2017.
 */

public class MovieTrailer implements Parcelable{

    private String id;
    // key to access the trailer in YouTube
    private String name;
    private String key;

    public MovieTrailer(String id, String name, String key) {
        this.id = id;
        this.name = name;
        this.key = key;
    }

    private MovieTrailer(Parcel in) {
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
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(key);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MovieTrailer> CREATOR
            = new Parcelable.Creator<MovieTrailer>() {
        public MovieTrailer createFromParcel(Parcel in) {
            return new MovieTrailer(in);
        }

        public MovieTrailer[] newArray(int size) {
            return new MovieTrailer[size];
        }
    };
}
