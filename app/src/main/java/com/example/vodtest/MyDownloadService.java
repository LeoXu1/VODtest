package com.example.vodtest;

import android.app.Notification;
import android.provider.MediaStore;

import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.Scheduler;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

public class MyDownloadService extends DownloadService {
    DatabaseProvider databaseProvider;

    public MyDownloadService() {
        super(1);
        databaseProvider = new ExoDatabaseProvider(MyDownloadService.this);
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
