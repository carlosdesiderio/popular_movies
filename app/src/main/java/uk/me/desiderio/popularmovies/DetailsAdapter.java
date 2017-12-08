package uk.me.desiderio.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.me.desiderio.popularmovies.data.MoviesContract.ReviewEntry;
import uk.me.desiderio.popularmovies.data.MoviesContract.TrailerEntry;
import uk.me.desiderio.popularmovies.view.DetailListViewItem;
import uk.me.desiderio.popularmovies.view.DetailListViewItem.ViewType;

import static uk.me.desiderio.popularmovies.view.DetailListViewItem.REVIEW_HEADING_ITEM_TYPE;
import static uk.me.desiderio.popularmovies.view.DetailListViewItem.REVIEW_ITEM_TYPE;
import static uk.me.desiderio.popularmovies.view.DetailListViewItem.TRAILER_HEADING_ITEM_TYPE;
import static uk.me.desiderio.popularmovies.view.DetailListViewItem.TRAILER_ITEM_TYPE;

/**
 * Adapter for the {@link DetailsActivity}
 */

class DetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = DetailsAdapter.class.getSimpleName();


    public interface OnItemClickListener {
        void onReviewSelected(String reviewUrlString);
        void onTrailerSelected(String trailerKey);
    }

    private OnItemClickListener itemClickListener;
    private final Context context;

    @Nullable
    private Cursor trailersCursor;
    @Nullable
    private Cursor reviewsCursor;
    private List<String> labels;
    private List<DetailListViewItem> viewItemList;

    DetailsAdapter(Context context) {
        this.context = context;
        labels = new ArrayList<>();
        viewItemList = new ArrayList<>();
    }

    @Nullable
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @ViewType int viewType) {
        View listItemView;
        switch (viewType) {
            case TRAILER_ITEM_TYPE:
                listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_list_trailer_item_layout, parent, false);
                return new DetailsAdapter.TrailerViewHolder(listItemView);
            case REVIEW_ITEM_TYPE:
                listItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_list_review_item_layout, parent, false);
                return new DetailsAdapter.ReviewViewHolder(listItemView);
            case TRAILER_HEADING_ITEM_TYPE:
            case REVIEW_HEADING_ITEM_TYPE:
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
            case TRAILER_HEADING_ITEM_TYPE:
            case REVIEW_HEADING_ITEM_TYPE:
                String headingLabel = labels.get(getItemDataOriginalPosition(position));
                HeadingViewHolder stringViewHolder = (HeadingViewHolder) holder;
                stringViewHolder.headingTextView.setText(headingLabel);
                break;
            case REVIEW_ITEM_TYPE:
                int dataOriginalPosition = getItemDataOriginalPosition(position);
                if (reviewsCursor != null && reviewsCursor.moveToPosition(dataOriginalPosition)) {

                    String content = reviewsCursor.getString(reviewsCursor.getColumnIndex(ReviewEntry.COLUMN_CONTENT));
                    final String url = reviewsCursor.getString(reviewsCursor.getColumnIndex(ReviewEntry.COLUMN_URL));

                    ReviewViewHolder reviewViewHolder = (ReviewViewHolder) holder;

                    reviewViewHolder.contentTextView.setText(content);
                    reviewViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (itemClickListener != null) {
                                itemClickListener.onReviewSelected(url);
                            }
                        }
                    });
                }
                break;
            case TRAILER_ITEM_TYPE:
                int trailerPosition = getItemDataOriginalPosition(position);
                if(trailersCursor != null && trailersCursor.moveToPosition(trailerPosition)) {

                    String name = trailersCursor.getString(trailersCursor.getColumnIndex(TrailerEntry.COLUMN_NAME));
                    final String key = trailersCursor.getString(trailersCursor.getColumnIndex(TrailerEntry.COLUMN_KEY));
                    TrailerViewHolder trailerViewHolder = (TrailerViewHolder) holder;

                    trailerViewHolder.nameTextView.setText(name);
                    trailerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (itemClickListener != null) {
                                itemClickListener.onTrailerSelected(key);
                            }
                        }
                    });
                }
                break;
        }
    }

    @Override
    @ViewType
    public int getItemViewType(int position) {
        DetailListViewItem item = viewItemList.get(position);
        return item.getType();
    }

    @Override
    public int getItemCount() {
        if (viewItemList != null) {
            return viewItemList.size();
        }
        return 0;
    }

    /**
     * It returns the first YouTube trailer key
     */
    @Nullable
    String getVideoSharingKey() {
        if(trailersCursor != null && trailersCursor.moveToPosition(0)) {
            return trailersCursor.getString(trailersCursor.getColumnIndex(TrailerEntry.COLUMN_KEY));
        }
        return null;
    }

    void registerOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    void swapTrailersCursor(@Nullable Cursor trailersCursor) {
        if (trailersCursor != null && trailersCursor.getCount() > 0) {
            labels.add(context.getString(R.string.details_list_heading_trailers));
            Log.d(TAG, "Swap Trailer Cursor :: Register Header View");
            registerDataTypeItem(TRAILER_HEADING_ITEM_TYPE, labels.size() - 1);
            this.trailersCursor = trailersCursor;
            Log.d(TAG, "Swap Trailer Cursor :: Register " + trailersCursor.getCount() + " Trailer View Items");
            registerAllCursorDataItems(TRAILER_ITEM_TYPE, trailersCursor);
            // sort list items so that the trailers are shown before the reviews
            Collections.sort(viewItemList, new DetailListViewItem.ViewItemComparator());
        }
        notifyDataSetChanged();
    }

    void swapReviewsCursor(@Nullable Cursor reviewsCursor) {
        if (reviewsCursor != null && reviewsCursor.getCount() > 0) {
            this.labels.add(context.getString(R.string.details_list_heading_reviews));
            Log.d(TAG, "Swap Trailer Cursor :: Register Header View");
            registerDataTypeItem(REVIEW_HEADING_ITEM_TYPE, labels.size() - 1);
            this.reviewsCursor = reviewsCursor;
            Log.d(TAG, "Swap Trailer Cursor :: Register " + reviewsCursor.getCount() + " Review View Items");
            // sort list items so that the reviews are shown after the trailers
            registerAllCursorDataItems(REVIEW_ITEM_TYPE, reviewsCursor);
        }
        notifyDataSetChanged();
    }

    void resetData() {
        this.trailersCursor = null;
        this.reviewsCursor = null;
        this.labels = new ArrayList<>();
        this.viewItemList = new ArrayList<>();
    }

    boolean hasData() {
        return (viewItemList != null && viewItemList.size() > 0);
    }

    private void registerDataTypeItem(@ViewType int type, int dataOriginalIndex) {
        DetailListViewItem dataItemType = new DetailListViewItem(type, dataOriginalIndex);
        viewItemList.add(dataItemType);
    }

    private void registerAllCursorDataItems(@ViewType int type, @NonNull Cursor cursor) {
        while(cursor.moveToNext()) {
            registerDataTypeItem(type, cursor.getPosition());
        }
    }

    private int getItemDataOriginalPosition(int adapterPosition) {
        return viewItemList.get(adapterPosition).getOriginalIndex();
    }

    public class HeadingViewHolder extends RecyclerView.ViewHolder {
        final TextView headingTextView;

        HeadingViewHolder(@NonNull View itemView) {
            super(itemView);
            headingTextView = itemView.findViewById(R.id.heading_content_text_view);
        }
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder {
        final TextView nameTextView;

        TrailerViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.trailer_name_text_view);
        }
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        final TextView contentTextView;


        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            contentTextView = itemView.findViewById(R.id.review_content_text_view);
        }
    }
}
