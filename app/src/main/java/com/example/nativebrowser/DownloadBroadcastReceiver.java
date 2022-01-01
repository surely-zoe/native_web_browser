package com.example.nativebrowser;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.DOWNLOAD_SERVICE;


public class DownloadBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            //Show a notification
            Toast.makeText(context, "Downloaded File!!!", Toast.LENGTH_LONG).show();
            Log.d("222222", "onReceive:");
        }
    }
}