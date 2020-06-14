package com.example.vodtest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.ListVideosQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private List<ListVideosQuery.Item> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private static final String TAG = MyAdapter.class.getSimpleName();
    private RecyclerViewClickListener mListener;



    // data is passed into the constructor
    SearchResultsAdapter(Context context, RecyclerViewClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        mListener = listener;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.search_result, parent, false);
        return new ViewHolder(view, mListener);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(mData.get(position));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // resets the list with a new set of data
    public void setItems(List<ListVideosQuery.Item> items) {
        mData = items;
    }

    // stores and recycles views as they are scrolled off screen
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_title;
        TextView txt_genre;
        TextView txt_duration;
        ImageView image_view;
        private RecyclerViewClickListener mListener;

        ViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);
            txt_title = itemView.findViewById(R.id.searchResultTitle);
            txt_genre = itemView.findViewById(R.id.searchResultGenre);
            txt_duration = itemView.findViewById(R.id.searchResultDuration);
            image_view = itemView.findViewById(R.id.searchResultThumb);
            mListener = listener;
            itemView.setOnClickListener(this);
        }

        void bindData(ListVideosQuery.Item item) {
            txt_title.setText(item.title());
            txt_genre.setText(item.genre());
            Integer duration = item.duration();
            int hours = duration/3600;
            int minutes = (duration - 3600*hours)/60;
            int seconds = duration - 3600*hours - 60*minutes;
            String timeString;
            if (hours != 0) {
                timeString = hours +":"+minutes+":"+seconds;
            } else {
                timeString = minutes+":"+seconds;
            }
            txt_duration.setText(timeString);
            if (item.thumbNailsUrls() == null) {
                //loading icon
                Picasso.get().load("https://i.stack.imgur.com/h6viz.gif").into(image_view);
            } else {
                Picasso.get().load(item.thumbNailsUrls().get(0)).into(image_view);
            }

        }
        @Override
        public void onClick(View v) {
            Log.d(TAG, "Clicked");
            mListener.onClick(v, getAdapterPosition());

        }
    }
    public interface RecyclerViewClickListener {

        void onClick(View view, int position);
    }
}
