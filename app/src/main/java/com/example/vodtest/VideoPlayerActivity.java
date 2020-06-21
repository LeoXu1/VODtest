package com.example.vodtest;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.amazonaws.amplify.generated.graphql.CreateReviewMutation;
import com.amazonaws.amplify.generated.graphql.GetUserQuery;
import com.amazonaws.amplify.generated.graphql.ListReviewsQuery;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.Scheduler;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import type.CreateReviewInput;

public class VideoPlayerActivity extends AppCompatActivity {

    boolean fullscreen = false;
    String id;
    String name;
    String location;
    SimpleExoPlayer player;
    DataSource.Factory dataSourceFactory;
    DatabaseProvider databaseProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        //Player
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        id = extras.getString("EXTRA_ID");
        String title = extras.getString("EXTRA_TITLE");
        String genre = extras.getString("EXTRA_GENRE");
        String url = extras.getString("EXTRA_URL");
        String sub = extras.getString("EXTRA_SUB");
        TextView titleText = findViewById(R.id.text_title);
        TextView genreText = findViewById(R.id.text_genre);
        PlayerView playerView = findViewById(R.id.player_view);
        titleText.setText(title);
        genreText.setText(genre);

        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        dataSourceFactory = new DefaultHttpDataSourceFactory(
                Util.getUserAgent(this, "VOD"));
        DownloadHelper downloadHelper = DownloadHelper.forHls(this,
                        Uri.parse(url),
                        dataSourceFactory,
                        new DefaultRenderersFactory(this));
        downloadHelper.prepare(downloadHelperCallback);


        //Downloads
        Button downloadButton = findViewById(R.id.btn_download);
        downloadButton.setTransformationMethod(null);
        downloadButton.setOnClickListener(view -> downloadVideo());

        //Fullscreen player
        ImageView fullscreenButton = playerView.findViewById(R.id.exo_fullscreen_icon);
        fullscreenButton.setOnClickListener(view -> {
            if(fullscreen) {
                fullscreenButton.setImageDrawable(ContextCompat.getDrawable(VideoPlayerActivity.this, R.drawable.ic_fullscreen_black_24dp));
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                if(getSupportActionBar() != null){
                    getSupportActionBar().show();
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) playerView.getLayoutParams();
                params.width = params.MATCH_PARENT;
                params.height = (int) ( 200 * getApplicationContext().getResources().getDisplayMetrics().density);
                playerView.setLayoutParams(params);
                fullscreen = false;
            }else{
                fullscreenButton.setImageDrawable(ContextCompat.getDrawable(VideoPlayerActivity.this, R.drawable.ic_fullscreen_exit_black_24dp));
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                        |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                if(getSupportActionBar() != null){
                    getSupportActionBar().hide();
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) playerView.getLayoutParams();
                params.width = params.MATCH_PARENT;
                params.height = params.MATCH_PARENT;
                playerView.setLayoutParams(params);
                fullscreen = true;
            }
        });

        GetUserQuery getUserQuery = GetUserQuery.builder()
                .id(sub)
                .build();

        ClientFactory.appSyncClient().query(getUserQuery)
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryCallback);

        LinearLayout profileInfo = findViewById(R.id.userInfoLayout);
        profileInfo.setOnClickListener(v -> {
            Intent viewProfileIntent = new Intent(VideoPlayerActivity.this, OtherProfileActivity.class);
            viewProfileIntent.putExtra("sub", sub);
            startActivity(viewProfileIntent);
        });

        Button reviewsButton = findViewById(R.id.btn_reviews);
        reviewsButton.setTransformationMethod(null);
        reviewsButton.setOnClickListener(v -> {
            Intent viewReviewsIntent = new Intent(VideoPlayerActivity.this, ReviewsActivity.class);
            viewReviewsIntent.putExtra("id", id);
            startActivity(viewReviewsIntent);
        });


    }

    @Override
    public void onBackPressed() {
        Log.i("VOD", "hello ffs");
        SharedPreferences prefs = getSharedPreferences("VOD", MODE_PRIVATE);
        prefs.edit().putLong("position"+id, player.getCurrentPosition()).apply();
        super.onBackPressed();
    }

    public void downloadVideo() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String mp4Url = extras.getString("EXTRA_MP4URL");
        String title = extras.getString("EXTRA_TITLE");
        Thread downloadThread = new Thread(() -> {
            try {
                URL url = new URL(mp4Url);
                String path = this.getFilesDir().getPath()+"/"+title+".mp4";
                FileUtils.copyURLToFile(url, new File(path));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        downloadThread.start();
    }
    private GraphQLCall.Callback<GetUserQuery.Data> queryCallback = new GraphQLCall.Callback<GetUserQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<GetUserQuery.Data> response) {

            runOnUiThread(() -> {
                ImageView profileImage = findViewById(R.id.profilePictureDisplay);
                TextView nameText = findViewById(R.id.text_displayName);
                TextView locationText = findViewById(R.id.text_displayLocation);
                name = response.data().getUser().name();
                nameText.setText(name);

                if (response.data().getUser().pictureUrl() != null) {
                    String picUrl = response.data().getUser().pictureUrl();
                    Picasso.get().load(picUrl).into(profileImage);
                } else {
                    Picasso.get().load("https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png").into(profileImage);
                }
                if (response.data().getUser().location() != null) {
                    location = response.data().getUser().location();
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
    private DownloadHelper.Callback downloadHelperCallback = new DownloadHelper.Callback() {
        @Override
        public void onPrepared(DownloadHelper helper) {
            DownloadRequest downloadRequest = helper.getDownloadRequest(null);
            DownloadService.sendAddDownload(VideoPlayerActivity.this, MyDownloadService.class, downloadRequest,false);
            MediaSource videoSource = DownloadHelper.createMediaSource(downloadRequest,dataSourceFactory);
            player.prepare(videoSource);
            SharedPreferences prefs = getSharedPreferences("VOD", MODE_PRIVATE);
            long position = prefs.getLong("position"+id,0);
            Log.i("VOD", Long.toString(position));
            player.seekTo(position);
            player.setPlayWhenReady(true);
        }

        @Override
        public void onPrepareError(DownloadHelper helper, IOException e) {
            e.printStackTrace();

        }
    };

    class MyDownloadService extends DownloadService {
        public MyDownloadService() {
            super(1);
            databaseProvider = new ExoDatabaseProvider(VideoPlayerActivity.this);
        }
        protected DownloadManager getDownloadManager() {
            // A download cache should not evict media, so should use a NoopCacheEvictor.
            SimpleCache downloadCache = new SimpleCache(
                    getCacheDir(),
                    new NoOpCacheEvictor(),
                    databaseProvider);

            // Create a factory for reading the data from the network.
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(this,"VOD"));

            // Create the download manager.
            DownloadManager downloadManager = new DownloadManager(
                    this,
                    databaseProvider,
                    downloadCache,
                    dataSourceFactory);
            return downloadManager;
        }
        protected Scheduler getScheduler() {
            return null;
        }
        protected Notification getForegroundNotification(List<Download> list) {
            return null;
        }
    }


}
