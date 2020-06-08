package com.example.vodtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.GetUserQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.squareup.picasso.Picasso;

import javax.annotation.Nonnull;

public class OtherProfileActivity extends AppCompatActivity {
    String sub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_profile);
        sub = getIntent().getStringExtra("sub");
        GetUserQuery getUserQuery = GetUserQuery.builder()
                .id(sub)
                .build();

        ClientFactory.appSyncClient().query(getUserQuery)
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);
    }

    private GraphQLCall.Callback<GetUserQuery.Data> queryCallback = new GraphQLCall.Callback<GetUserQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<GetUserQuery.Data> response) {

            runOnUiThread(() -> {
                ImageView profileImage = findViewById(R.id.otherprofile_profilePicture);
                TextView nameText = findViewById(R.id.otherprofile_textName);
                TextView locationText = findViewById(R.id.otherprofile_textLocation);
                String name = response.data().getUser().name();
                nameText.setText(name);

                if (response.data().getUser().pictureUrl() != null) {
                    String picUrl = response.data().getUser().pictureUrl();
                    Picasso.get().load(picUrl).into(profileImage);
                } else {
                    Picasso.get().load("https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png").into(profileImage);
                }
                if (response.data().getUser().location() != null) {
                    String location = response.data().getUser().location();
                    locationText.setText(location);
                } else {
                    String locationStr = "Location not specified";
                    locationText.setText(locationStr);
                }
            });

        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("VOD", e.toString());
        }
    };
}