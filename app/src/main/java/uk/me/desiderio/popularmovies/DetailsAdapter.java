package uk.me.desiderio.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import uk.me.desiderio.popularmovies.data.MoviesContract.ReviewEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.TrailerEntry;

/**
 * Adapter for the {@link DetailsActivity}
 */

public class DetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemClickListener {
        void onReviewSelected(MovieReview review);
        void onTrailerSelected(MovieTrailer trailer);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HEADING_ITEM_TYPE, TRAILER_ITEM_TYPE, REVIEW_ITEM_TYPE})
    @interface ViewType { }
    private static final int HEADING_ITEM_TYPE = 1;
    private static final int TRAILER_ITEM_TYPE = 2;
    private static final int REVIEW_ITEM_TYPE = 3;

    // TODO revise this so to move it out as a independent class
    public class ViewItem {
        private int originalIndex;
        private int type;


        private ViewItem(@ViewType int type, int originalIndex) {
            this.type = type;
            this.originalIndex = originalIndex;
        }

        public int getOriginalIndex() {
            return originalIndex;
        }

        public int getType() {
            return type;
        }
    }

    private OnItemClickListener listener;
    private Context context;

    private Cursor trailersCursor;
    private Cursor reviewsCursor;
    private List<String> labels;
    private List<ViewItem> viewItemList;

    DetailsAdapter(Context context) {
        this.context = context;
        labels = new ArrayList<>();
        viewItemList = new ArrayList<>();
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
        @ViewType
        int viewType = getItemViewType(position);

        switch (viewType) {
            case HEADING_ITEM_TYPE:
                String headingLabel = labels.get(getItemDataPosition(position));
                HeadingViewHolder stringViewHolder = (HeadingViewHolder) holder;
                stringViewHolder.headingTextView.setText(headingLabel);
                break;
            case REVIEW_ITEM_TYPE:
                int reviewPosition = getItemDataPosition(position);
                boolean hasPosition = reviewsCursor.moveToPosition(reviewPosition);
                final MovieReview review = getMovieReviewFrom(reviewsCursor);
                ReviewViewHolder reviewViewHolder = (ReviewViewHolder) holder;
                if(review==null) return;
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
                int trailerPosition = getItemDataPosition(position);
                boolean hasPosition2 = trailersCursor.moveToPosition(trailerPosition);
                final MovieTrailer trailer = getMovieTrailerFrom(trailersCursor);
                TrailerViewHolder trailerViewHolder = (TrailerViewHolder) holder;
                if(trailer==null) return;
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

    @Override
    @ViewType
    public int getItemViewType(int position) {
        ViewItem item = viewItemList.get(position);
        return item.getType();
    }

    @Override
    public int getItemCount() {
        if (viewItemList != null) {
            return viewItemList.size();
        }
        return 0;
    }

    void registerListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    void addMovieTrailerData(Cursor trailersCursor) {
        if (trailersCursor != null && trailersCursor.getCount() > 0) {
            labels.add(context.getString(R.string.details_list_heading_trailers));
            registerDataTypeItem(HEADING_ITEM_TYPE, labels.size() - 1);
            this.trailersCursor = trailersCursor;
            registerAllCursorDataItems(TRAILER_ITEM_TYPE, trailersCursor);
        }
        notifyDataSetChanged();

        for (int i = 0; i < viewItemList.size(); i++) {
            ViewItem item = viewItemList.get(i);
            Log.d("CUSOR -----", "item type : " + item.getType() + " -- item original index: " + item.getOriginalIndex());
        }
        Log.d("CUSOR -----", "item type : =========================");
    }

    void addMovieReviewData(Cursor reviewsCursor) {
        if (reviewsCursor != null && reviewsCursor.getCount() > 0) {
            this.labels.add(context.getString(R.string.details_list_heading_reviews));
            registerDataTypeItem(HEADING_ITEM_TYPE, labels.size() - 1);
            this.reviewsCursor = reviewsCursor;
            registerAllCursorDataItems(REVIEW_ITEM_TYPE, reviewsCursor);
        }
        notifyDataSetChanged();

        for (int i = 0; i < viewItemList.size(); i++) {
            ViewItem item = viewItemList.get(i);
            Log.d("CUSOR -----", "item type : " + item.getType() + " -- item original index: " + item.getOriginalIndex());
        }
        Log.d("CUSOR -----", "item type : =========================");
    }

    // TODO revise creational strategy: when to reset
    void resetData() {
        this.trailersCursor = null;
        this.reviewsCursor = null;
        this.labels = new ArrayList<>();
        this.viewItemList = new ArrayList<>();
    }

    private void registerDataTypeItem(@ViewType int type, int dataOriginalIndex) {
        ViewItem dataItemType = new ViewItem(type, dataOriginalIndex);
        viewItemList.add(dataItemType);
    }

    private void registerAllCursorDataItems(@ViewType int type, Cursor cursor) {
        while(cursor.moveToNext()) {
            registerDataTypeItem(type, cursor.getPosition());
        }
    }

    private int getItemDataPosition(int adapterPosition) {
        return viewItemList.get(adapterPosition).getOriginalIndex();
    }

    private MovieReview getMovieReviewFrom(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_REVIEW_ID));
        String author = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_AUTHOR));
        String content = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_CONTENT));
        String url = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_URL));

        return new MovieReview(id, author, content, url);
    }
    private MovieTrailer getMovieTrailerFrom(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(TrailerEntry.COLUMN_TRAILER_ID));
        String name = cursor.getString(cursor.getColumnIndex(TrailerEntry.COLUMN_NAME));
        String key = cursor.getString(cursor.getColumnIndex(TrailerEntry.COLUMN_KEY));

        return new MovieTrailer(id, name, key);
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
