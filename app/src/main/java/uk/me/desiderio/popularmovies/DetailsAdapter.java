package uk.me.desiderio.popularmovies;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import uk.me.desiderio.popularmovies.data.MovieReview;
import uk.me.desiderio.popularmovies.data.MovieTrailer;

/**
 * Adapter for the {@link DetailsActivity}
 */

public class DetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final int HEADING_ITEM_TYPE = 1;
    private static final int TRAILER_ITEM_TYPE = 2;
    private static final int REVIEW_ITEM_TYPE = 3;
    private OnItemClickListener listener;
    private List<Object> data = new ArrayList<>();
    private Context context;
    DetailsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @ViewType int viewType) {
        View listItemView;
        switch (viewType) {
            case TRAILER_ITEM_TYPE:
                listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_list_trailer_item_layout, parent, false);
                return new DetailsAdapter.TrailerViewHolder(listItemView);
            case REVIEW_ITEM_TYPE:
                listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_list_review_item_layout, parent, false);
                return new DetailsAdapter.ReviewViewHolder(listItemView);
            case HEADING_ITEM_TYPE:
                listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_list_heading_item_layout, parent, false);
                return new DetailsAdapter.HeadingViewHolder(listItemView);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case HEADING_ITEM_TYPE:
                String headingLabel = (String) data.get(position);
                HeadingViewHolder stringViewHolder = (HeadingViewHolder) holder;
                stringViewHolder.headingTextView.setText(headingLabel);
                break;
            case REVIEW_ITEM_TYPE:
                final MovieReview review = (MovieReview) data.get(position);
                ReviewViewHolder reviewViewHolder = (ReviewViewHolder) holder;
                reviewViewHolder.contentTextView.setText(review.getContent());
                reviewViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            listener.onReviewSelected(review);
                        }
                    }
                });
                break;
            case TRAILER_ITEM_TYPE:
                final MovieTrailer trailer = (MovieTrailer) data.get(position);
                TrailerViewHolder trailerViewHolder = (TrailerViewHolder) holder;
                trailerViewHolder.nameTextView.setText(trailer.getName());
                trailerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (listener != null) {
                            listener.onTrailerSelected(trailer);
                        }
                    }
                });
                break;
        }
    }

    private void addData(List<? extends Object> data) {
        this.data.addAll(data);
        notifyDataSetChanged();
    }

    void addTrailerData(List<MovieTrailer> trailers) {
        if (trailers != null && trailers.size() > 0) {
            this.data.add(context.getString(R.string.details_list_heading_trailers));
            addData(trailers);
        }
    }

    void addReviewData(List<MovieReview> reviews) {
        if (reviews != null && reviews.size() > 0) {
            this.data.add(context.getString(R.string.details_list_heading_reviews));
            addData(reviews);
        }
    }

    void registerListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    @ViewType
    public int getItemViewType(int position) {
        Object dataItem = data.get(position);
        if (dataItem instanceof String) {
            return HEADING_ITEM_TYPE;
        } else if (dataItem instanceof MovieTrailer) {
            return TRAILER_ITEM_TYPE;
        } else if (dataItem instanceof MovieReview) {
            return REVIEW_ITEM_TYPE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        }
        return 0;
    }

    public interface OnItemClickListener {
        void onReviewSelected(MovieReview review);

        void onTrailerSelected(MovieTrailer trailer);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HEADING_ITEM_TYPE, TRAILER_ITEM_TYPE, REVIEW_ITEM_TYPE})
    @interface ViewType {
    }

    public class HeadingViewHolder extends RecyclerView.ViewHolder {
        TextView headingTextView;

        HeadingViewHolder(View itemView) {
            super(itemView);
            headingTextView = itemView.findViewById(R.id.heading_content_text_view);
        }
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;

        TrailerViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.trailer_name_text_view);
        }
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView contentTextView;


        ReviewViewHolder(View itemView) {
            super(itemView);
            contentTextView = itemView.findViewById(R.id.review_content_text_view);
        }
    }
}
