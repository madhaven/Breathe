package com.example.breathe;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import static com.example.breathe.MainActivity.TAG;
import static com.example.breathe.MainActivity.testIntervalCount;

public class AlarmReceiver extends BroadcastReceiver {

    public NotificationManager notificationManager;
//    public Notifi

    @Override
    public void onReceive(Context context, Intent intent) {
        String theMessage = "BroadCast Received : "+ String.valueOf(++testIntervalCount);
        Log.d(TAG, theMessage);
        Toast.makeText(context, theMessage, Toast.LENGTH_SHORT).show();
        // TODO: send notification with variables from main
    }
}
