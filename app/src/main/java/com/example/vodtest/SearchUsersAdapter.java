package com.example.vodtest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.amplify.generated.graphql.ListVideosQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersAdapter extends RecyclerView.Adapter<SearchUsersAdapter.ViewHolder> {

    private List<ListUsersQuery.Item> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private static final String TAG = MyAdapter.class.getSimpleName();
    private RecyclerViewClickListener mListener;



    // data is passed into the constructor
    SearchUsersAdapter(Context context, RecyclerViewClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        mListener = listener;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.search_user, parent, false);
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
    public void setItems(List<ListUsersQuery.Item> items) {
        mData = items;
    }

    // stores and recycles views as they are scrolled off screen
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_name;
        TextView txt_location;
        ImageView image_view;
        private RecyclerViewClickListener mListener;

        ViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);
            txt_name = itemView.findViewById(R.id.searchUserName);
            txt_location = itemView.findViewById(R.id.searchUserLocation);
            image_view = itemView.findViewById(R.id.searchUserPfP);
            mListener = listener;
            itemView.setOnClickListener(this);
        }

        void bindData(ListUsersQuery.Item item) {
            txt_name.setText(item.name());
            String locationText;
            if (item.location() == null) {
                locationText = "No location";
            } else {
                locationText = item.location();
            }
            txt_location.setText(locationText);
            if (item.pictureUrl() == null) {
                //loading icon
                Picasso.get().load("https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png").into(image_view);
            } else {
                Picasso.get().load(item.pictureUrl()).into(image_view);
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
