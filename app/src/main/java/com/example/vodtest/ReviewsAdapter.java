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

import com.amazonaws.amplify.generated.graphql.ListReviewsQuery;
import com.amazonaws.amplify.generated.graphql.ListVideosQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {

    private List<ListReviewsQuery.Item> mData = new ArrayList<>();;
    private LayoutInflater mInflater;
    private static final String TAG = MyAdapter.class.getSimpleName();



    // data is passed into the constructor
    ReviewsAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.review, parent, false);
        return new ViewHolder(view);
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
    public void setItems(List<ListReviewsQuery.Item> items) {
        mData = items;
    }

    // stores and recycles views as they are scrolled off screen
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt_numStars;
        TextView txt_content;

        ViewHolder(View itemView) {
            super(itemView);
            txt_numStars = itemView.findViewById(R.id.reviewStars);
            txt_content = itemView.findViewById(R.id.reviewContent);
        }

        void bindData(ListReviewsQuery.Item item) {
            txt_numStars.setText(Integer.toString(item.stars()));
            txt_content.setText(item.content());

        }

    }
}
