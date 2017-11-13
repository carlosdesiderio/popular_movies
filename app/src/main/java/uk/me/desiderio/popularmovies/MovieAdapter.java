package uk.me.desiderio.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import uk.me.desiderio.popularmovies.data.Movie;

/**
 * Adapter class for the {@link RecyclerView} at the {@link MainActivity}
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Movie movie);
    }


    private List<Movie> movies;
    private OnItemClickListener onItemClickListener;



    public MovieAdapter() {
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_list_item_layout, parent, false);
        return new MovieViewHolder(listItemView);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        final Movie movie = movies.get(position);
        holder.titleTextView.setText(movie.getTitle());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null) {
                    onItemClickListener.onItemClick(movie);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void setData(List<Movie> movies) {
        this.movies = movies;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder {
        public final TextView titleTextView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            this.titleTextView = itemView.findViewById(R.id.movie_title_tv);
        }
    }
}
