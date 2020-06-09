package com.example.vodtest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateReviewMutation;
import com.amazonaws.amplify.generated.graphql.GetUserQuery;
import com.amazonaws.amplify.generated.graphql.GetVideoQuery;
import com.amazonaws.amplify.generated.graphql.ListReviewsQuery;
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
import type.ModelReviewFilterInput;
import type.ModelStringInput;
import type.ModelVideoFilterInput;

public class ReviewsActivity extends AppCompatActivity {

    private ArrayList<ListReviewsQuery.Item> reviews;
    RecyclerView recycler;
    ReviewsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        String id = getIntent().getStringExtra("id");

        recycler = findViewById(R.id.reviewsRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));


        // specify an adapter (see also next example)
        adapter = new ReviewsAdapter(this);
        recycler.setAdapter(adapter);

        Button writeReviewButton = findViewById(R.id.btn_writeReview);
        writeReviewButton.setTransformationMethod(null);
        writeReviewButton.setOnClickListener(v -> {
            Intent writeReviewIntent = new Intent(ReviewsActivity.this, WriteReviewActivity.class);
            writeReviewIntent.putExtra("id", id);
            startActivity(writeReviewIntent);
        });


        ModelStringInput idInput = ModelStringInput.builder().eq(id).build();
        ModelReviewFilterInput filterInput = ModelReviewFilterInput.builder().videoID(idInput).build();
        ClientFactory.appSyncClient().query(ListReviewsQuery.builder().filter(filterInput).build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);


    }
    private GraphQLCall.Callback<ListReviewsQuery.Data> queryCallback = new GraphQLCall.Callback<ListReviewsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListReviewsQuery.Data> response) {

            reviews = new ArrayList<>(response.data().listReviews().items());
            Log.i("VOD", reviews.toString());

            runOnUiThread(() -> {
                adapter.setItems(reviews);
                adapter.notifyDataSetChanged();

            });

        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("VOD", e.toString());
        }
    };

}