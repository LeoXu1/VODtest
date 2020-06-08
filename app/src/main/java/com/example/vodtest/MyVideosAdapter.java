package com.example.vodtest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.ListVideosQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MyVideosAdapter extends RecyclerView.Adapter<MyVideosAdapter.ViewHolder> {

    private List<ListVideosQuery.Item> mData = new ArrayList<>();;
    private LayoutInflater mInflater;
    private static final String TAG = MyAdapter.class.getSimpleName();
    private RecyclerViewClickListener mListener;



    // data is passed into the constructor
    MyVideosAdapter(Context context, RecyclerViewClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        mListener = listener;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.myvideos_view, parent, false);
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
        ImageView image_view;
        private RecyclerViewClickListener mListener;

        ViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);
            txt_title = itemView.findViewById(R.id.myVideoTitle);
            image_view = itemView.findViewById(R.id.myVideoThumb);
            mListener = listener;
            itemView.setOnClickListener(this);
        }

        void bindData(ListVideosQuery.Item item) {
            txt_title.setText(item.title());
            if (item.thumbNailsUrls() == null) {
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
