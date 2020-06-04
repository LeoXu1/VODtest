package com.example.vodtest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;

public class VideoPlayerActivity extends AppCompatActivity {

    boolean fullscreen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        //Player
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String title = extras.getString("EXTRA_TITLE");
        String genre = extras.getString("EXTRA_GENRE");
        String url = extras.getString("EXTRA_URL");
        TextView titleText = findViewById(R.id.text_title);
        TextView genreText = findViewById(R.id.text_genre);
        PlayerView playerView = findViewById(R.id.player_view);
        titleText.setText(title);
        genreText.setText(genre);
        SimpleExoPlayer player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "VOD"));
        MediaSource videoSource =
                new HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(url));
        player.prepare(videoSource);

        //Downloads
        Button downloadButton = findViewById(R.id.btn_download);
        downloadButton.setTransformationMethod(null);
        downloadButton.setOnClickListener(view -> downloadVideo());

        //Fullscreen player
        ImageView fullscreenButton = playerView.findViewById(R.id.exo_fullscreen_icon);
        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SourceLockedOrientationActivity")
            @Override
            public void onClick(View view) {
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
            }
        });

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
}
