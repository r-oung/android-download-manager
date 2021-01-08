package com.hrst.download_manager;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import static com.hrst.download_manager.Utilities.getMimeFromFileName;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private long downloadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(onDownloadComplete);
    }

    /**
     * Download-Button callback
     * @param v Button view
     */
    public void onDownload(View v) {
        String url = "https://github.com/hapi-robo/WiFiAnalyzer/releases/download/V3.0.2-TEMI/wifi_analyzer-v3.0.2-temi.apk";
        String fileName = url.substring(url.lastIndexOf('/') + 1);

        // https://developer.android.com/reference/android/app/DownloadManager.Request
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, fileName)
                .setTitle(fileName)
                .setMimeType(getMimeFromFileName(fileName));

        DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        downloadId = dm.enqueue(request); // add download request to the queue
    }

    /**
     * Broadcast receiver for handling ACTION_DOWNLOAD_COMPLETE intents
     */
    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the download ID received with the broadcast
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            // Check if the ID matches our download ID
            if (downloadId == id) {
                Log.i(TAG, "Download ID: " + downloadId);

                // Get file URI
                DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor c = dm.query(query);
                if (c.moveToFirst()) {
                    int colIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(colIndex)) {
                        Log.i(TAG, "Download Complete");
                        Toast.makeText(MainActivity.this, "Download Complete", Toast.LENGTH_SHORT).show();

                        String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        Log.i(TAG, "URI: " + uriString);
                    } else {
                        Log.w(TAG, "Download Unsuccessful, Status Code: " + c.getInt(colIndex));
                        Toast.makeText(MainActivity.this, "Download Unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };
}