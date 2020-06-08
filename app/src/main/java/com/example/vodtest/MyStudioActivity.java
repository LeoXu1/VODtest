package com.example.vodtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.GetUserQuery;
import com.amazonaws.amplify.generated.graphql.GetVideoQuery;
import com.amazonaws.amplify.generated.graphql.ListVideosQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import type.ModelIDInput;
import type.ModelStringInput;
import type.ModelVideoFilterInput;

public class MyStudioActivity extends AppCompatActivity {

    private ArrayList<ListVideosQuery.Item> videos;
    RecyclerView recycler;
    MyVideosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_studio);

        recycler = findViewById(R.id.myVideoRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        //Set up the video player when a video is clicked
        MyVideosAdapter.RecyclerViewClickListener mListener = (view, position) -> {
            String title = videos.get(position).title();
            String genre = videos.get(position).genre();
            String hlsUrl = videos.get(position).hlsUrl();
            String mp4Url = videos.get(position).mp4Urls().get(0);
            String sub = videos.get(position).sub();
            Intent playVideoIntent = new Intent(MyStudioActivity.this, VideoPlayerActivity.class);
            Bundle extras = new Bundle();
            extras.putString("EXTRA_TITLE", title);
            extras.putString("EXTRA_GENRE", genre);
            extras.putString("EXTRA_URL", hlsUrl);
            extras.putString("EXTRA_MP4URL", mp4Url);
            extras.putString("EXTRA_SUB", sub);
            playVideoIntent.putExtras(extras);
            startActivity(playVideoIntent);
        };

        // specify an adapter (see also next example)
        adapter = new MyVideosAdapter(this, mListener);
        recycler.setAdapter(adapter);

        String sub = getIntent().getStringExtra("sub");
        ModelStringInput subInput = ModelStringInput.builder().eq(sub).build();
        ModelVideoFilterInput filterInput = ModelVideoFilterInput.builder().sub(subInput).build();
        ClientFactory.appSyncClient().query(ListVideosQuery.builder().filter(filterInput).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);


    }
    private GraphQLCall.Callback<ListVideosQuery.Data> queryCallback = new GraphQLCall.Callback<ListVideosQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListVideosQuery.Data> response) {

            videos = new ArrayList<>(response.data().listVideos().items());
            Log.i("VOD", videos.toString());

            runOnUiThread(() -> {
                adapter.setItems(videos);
                adapter.notifyDataSetChanged();

            });

        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("VOD", e.toString());
        }
    };
}