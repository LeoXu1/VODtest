package com.example.vodtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.AmazonWebServiceResponse;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.amplify.generated.graphql.GetUserQuery;
import com.amazonaws.amplify.generated.graphql.GetVideoQuery;
import com.amazonaws.amplify.generated.graphql.ListUsersQuery;
import com.amazonaws.amplify.generated.graphql.ListVideosQuery;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.AmazonHttpClient;
import com.amazonaws.http.ExecutionContext;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.http.HttpResponseHandler;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity {

    int numResults;
    ArrayList<ListVideosQuery.Item> mVideos = new ArrayList<>();
    ArrayList<ListUsersQuery.Item> mUsers = new ArrayList<>();
    RecyclerView mRecyclerView;
    RecyclerView mUserRecycler;
    SearchResultsAdapter mAdapter;
    SearchUsersAdapter mUserAdapter;
    EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mRecyclerView = findViewById(R.id.searchResultsRecycler);
        mUserRecycler = findViewById(R.id.searchUsersRecycler);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mUserRecycler.setLayoutManager(new LinearLayoutManager(this));

        //Set up the video player when a video is clicked
        SearchResultsAdapter.RecyclerViewClickListener mListener = (view, position) -> {
            String id = mVideos.get(position).id();
            String title = mVideos.get(position).title();
            String genre = mVideos.get(position).genre();
            String hlsUrl = mVideos.get(position).hlsUrl();
            String mp4Url = mVideos.get(position).mp4Urls().get(0);
            String sub = mVideos.get(position).sub();
            Intent playVideoIntent = new Intent(SearchActivity.this, VideoPlayerActivity.class);
            Bundle extras = new Bundle();
            extras.putString("EXTRA_ID", id);
            extras.putString("EXTRA_TITLE", title);
            extras.putString("EXTRA_GENRE", genre);
            extras.putString("EXTRA_URL", hlsUrl);
            extras.putString("EXTRA_MP4URL", mp4Url);
            extras.putString("EXTRA_SUB", sub);
            playVideoIntent.putExtras(extras);
            startActivity(playVideoIntent);
        };

        SearchUsersAdapter.RecyclerViewClickListener mUserListener = (view, position) -> {
            String sub = mUsers.get(position).id();
            Intent viewProfileIntent = new Intent(SearchActivity.this, OtherProfileActivity.class);
            viewProfileIntent.putExtra("sub", sub);
            startActivity(viewProfileIntent);
        };

        // specify an adapter (see also next example)
        mAdapter = new SearchResultsAdapter(this, mListener);
        mRecyclerView.setAdapter(mAdapter);
        mUserAdapter = new SearchUsersAdapter(this, mUserListener);
        mUserRecycler.setAdapter(mUserAdapter);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_home:
                    Intent a = new Intent(SearchActivity.this, HomeActivity.class);
                    startActivity(a);
                    overridePendingTransition(0, 0);
                    break;
                case R.id.action_profile:
                    Intent b = new Intent(SearchActivity.this, MyProfileActivity.class);
                    startActivity(b);
                    overridePendingTransition(0, 0);
                    break;
                case R.id.action_search:

                    break;
            }
            return true;
        });
        MenuItem item = navigation.getMenu().findItem(R.id.action_search);
        item.setChecked(true);

        searchBar = findViewById(R.id.searchBar);

        ImageButton searchButton = findViewById(R.id.btn_search);

        searchButton.setOnClickListener(v -> {
            mVideos.clear();
            mUsers.clear();
            getResults(searchBar.getText().toString());
        });


    }

    public void getResults(String query) {
        try {

            ElasticRestClient.get("search?q="+query, null, new JsonHttpResponseHandler() { // instead of 'get' use twitter/tweet/1
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    try {
                        JSONObject hits = response.getJSONObject("hits");
                        JSONArray results = hits.getJSONArray("hits");
                        numResults = results.length();
                        for (int i = 0; i < numResults; i++) {
                            if (results.getJSONObject(i).getJSONObject("_source").get("__typename").equals("Video")) {
                                String title = (String) results.getJSONObject(i).getJSONObject("_source").get("title");
                                String genre = (String) results.getJSONObject(i).getJSONObject("_source").get("genre");
                                String url = (String) results.getJSONObject(i).getJSONObject("_source").get("hlsUrl");
                                String sub = (String) results.getJSONObject(i).getJSONObject("_source").get("sub");
                                JSONArray thumbUrls = results.getJSONObject(i).getJSONObject("_source").getJSONArray("thumbNailsUrls");
                                JSONArray mp4Urls = results.getJSONObject(i).getJSONObject("_source").getJSONArray("mp4Urls");
                                int duration = (int) results.getJSONObject(i).getJSONObject("_source").get("duration");
                                String id = (String) results.getJSONObject(i).get("_id");
                                String time = ZonedDateTime
                                        .now(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));
                                ArrayList<String> thumbUrlsList = new ArrayList<String>();
                                for (int j = 0; j < thumbUrls.length(); j++) {
                                    thumbUrlsList.add(thumbUrls.get(j).toString());
                                }
                                ArrayList<String> mp4UrlsList = new ArrayList<String>();
                                for (int j = 0; j < mp4Urls.length(); j++) {
                                    mp4UrlsList.add(mp4Urls.get(j).toString());
                                }
                                ListVideosQuery.Item film = new ListVideosQuery.Item("Video",
                                        id, title, genre, url, thumbUrlsList, mp4UrlsList, sub, duration, time, time);
                                mVideos.add(film);
                            } else if (results.getJSONObject(i).getJSONObject("_source").get("__typename").equals("User")) {
                                String name = (String) results.getJSONObject(i).getJSONObject("_source").get("name");
                                String userId = (String) results.getJSONObject(i).get("_id");
                                String time = ZonedDateTime
                                        .now(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss"));
                                String location;
                                String picUrl;
                                try {
                                    location = (String) results.getJSONObject(i).getJSONObject("_source").get("location");
                                    picUrl = (String) results.getJSONObject(i).getJSONObject("_source").get("pictureUrl");
                                } catch (JSONException e) {
                                    location = "Location not specified";
                                    picUrl = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png";
                                }
                                ListUsersQuery.Item user = new ListUsersQuery.Item("User",userId, name,
                                        location,picUrl,time,time);
                                mUsers.add(user);
                            }
                        }
                        runOnUiThread(() -> {
                            mAdapter.setItems(mVideos);
                            mAdapter.notifyDataSetChanged();
                            mUserAdapter.setItems(mUsers);
                            mUserAdapter.notifyDataSetChanged();
                            TextView numResultsText = findViewById(R.id.numResults);
                            String resultsText = "Found "+ numResults +" results.";
                            numResultsText.setText(resultsText);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                    Log.i("VOD", "JSON Array" + response.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                    Log.e("VOD", "onFailure");
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                }

                @Override
                public void onRetry(int retryNo) {
                    Log.i("VOD", "onRetry " + retryNo);
                    // called when request is retried
                }
            });
        }
        catch (Exception e){
            Log.e("VOD", e.getLocalizedMessage());
        }
    }

}