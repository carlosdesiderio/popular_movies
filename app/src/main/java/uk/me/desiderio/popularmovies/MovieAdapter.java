package uk.me.desiderio.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import uk.me.desiderio.popularmovies.data.Movie;
import uk.me.desiderio.popularmovies.data.MoviesContract.MoviesEntry;
import uk.me.desiderio.popularmovies.network.MovieDatabaseRequestUtils;

/**
 * Adapter class for the {@link RecyclerView} at the {@link MainActivity}
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private static final String TAG = MovieAdapter.class.getSimpleName();

    public interface OnItemClickListener {
        void onItemClick(Movie movie, View sharedView);
    }

    private Cursor moviesCursor;
    private OnItemClickListener onItemClickListener;

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_list_item_layout, parent, false);
        return new MovieViewHolder(listItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MovieViewHolder holder, int position) {
        if(!moviesCursor.moveToPosition(position)) {
            return;
        }
        final Movie movie = getMovieObjectFromCursor();

        final Uri imageUri = MovieDatabaseRequestUtils.getMoviePosterUri(movie.getPosterURLPathString());

        Log.d(TAG, "onBindViewHolder :: image uri: " + imageUri.toString());
        Picasso.with(holder.itemView.getContext()).load(imageUri).into(holder.posterImageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(movie, holder.posterImageView);
                }
            }
        });
    }

    private Movie getMovieObjectFromCursor() {
        int id = moviesCursor.getInt(moviesCursor.getColumnIndex(MoviesEntry.COLUMN_MOVIE_ID));
        String title = moviesCursor.getString(moviesCursor.getColumnIndex(MoviesEntry.COLUMN_TITLE));
        String date = moviesCursor.getString(moviesCursor.getColumnIndex(MoviesEntry.COLUMN_DATE));
        String synopsys = moviesCursor.getString(moviesCursor.getColumnIndex(MoviesEntry.COLUMN_SYNOPSIS));
        double voteAverage = moviesCursor.getDouble(moviesCursor.getColumnIndex(MoviesEntry.COLUMN_VOTE_AVERAGE));
        String moviePosterUri = moviesCursor.getString(moviesCursor.getColumnIndex(MoviesEntry.COLUMN_POSTER_URL));

        return new Movie(id, title, date, synopsys, voteAverage, moviePosterUri);
    }

    @Override
    public int getItemCount() {
        if (moviesCursor != null) {
            return moviesCursor.getCount();
        }
        return 0;
    }

    void swapCursor(Cursor moviesCursor) {
        this.moviesCursor = moviesCursor;
        notifyDataSetChanged();
    }

    boolean hasData() {
        return moviesCursor != null && moviesCursor.getCount() > 0;
    }

    void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {
        final ImageView posterImageView;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            this.posterImageView = itemView.findViewById(R.id.movie_poster_image_view);
        }
    }
}
