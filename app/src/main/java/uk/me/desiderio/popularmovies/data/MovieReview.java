package uk.me.desiderio.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Data object to hold information about a movie review
 */

public class MovieReview implements Parcelable {

    private String id;
    private String author;
    private String content;
    private String url;

    public MovieReview(String id, String author, String content, String url) {

        this.id = id;
        this.author = author;
        this.content = content;
        this.url = url;
    }

    private MovieReview(Parcel in) {
        id = in.readString();
        author = in.readString();
        content = in.readString();
        url = in.readString();
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(author);
        parcel.writeString(content);
        parcel.writeString(url);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MovieReview> CREATOR
            = new Parcelable.Creator<MovieReview>() {
        public MovieReview createFromParcel(Parcel in) {
            return new MovieReview(in);
        }

        public MovieReview[] newArray(int size) {
            return new MovieReview[size];
        }
    };
}
