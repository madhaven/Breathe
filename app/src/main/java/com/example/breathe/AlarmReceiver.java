package com.example.breathe;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.example.breathe.MainActivity.TAG;
import static com.example.breathe.MainActivity.alarmIntentRequestCode;
import static com.example.breathe.MainActivity.autoCancelPreferenceString;
import static com.example.breathe.MainActivity.breatheAlarm;
import static com.example.breathe.MainActivity.breatheTogglePreferenceString;
import static com.example.breathe.MainActivity.interval;
import static com.example.breathe.MainActivity.notificationBreatheChannelIDString;
import static com.example.breathe.MainActivity.notificationTimeOut;
import static com.example.breathe.MainActivity.preferencesUnitedString;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Broadcast Received");

        String intention = intent.getStringExtra("Intention");
        if (intention == null) intention = "nothing";
        switch (intention) {
            case "startAlarm":
                Log.d(TAG, "starting alarm");
                startAlarms(context);
                break;
            case "stopAlarm":
                Log.d(TAG, "stopping alarm");
                stopAlarms(context);
                break;
            default:
                Log.d(TAG, "sending notification");
                sendNotification(context);
        }
//        Toast.makeText(context, theMessage, Toast.LENGTH_SHORT).show();
    }

    private void startAlarms(Context context) {
        breatheAlarm.setRepeating(
                AlarmManager.RTC_WAKEUP,
                0,
                interval * 60 * 1000,
                PendingIntent.getBroadcast(
                        context,
                        alarmIntentRequestCode,
                        new Intent(context, AlarmReceiver.class),
                        PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT
                )
        );

//        toggleBreathe(true);
        SharedPreferences preferences = context.
                getSharedPreferences(
                        preferencesUnitedString,
                        Context.MODE_PRIVATE
                );
        @SuppressLint("CommitPrefEdits") final SharedPreferences.Editor preferenceEditor = preferences.edit();
        if (preferences.contains(breatheTogglePreferenceString)) {
            preferenceEditor.putBoolean(breatheTogglePreferenceString, true);
            preferenceEditor.apply();
        }

    }

    private void stopAlarms(Context context) {
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                context,
                alarmIntentRequestCode,
                new Intent(context, AlarmReceiver.class),
                PendingIntent.FLAG_CANCEL_CURRENT
        );
        if (breatheAlarm != null && alarmIntent != null) {
            breatheAlarm.cancel(alarmIntent);
            Log.d(TAG, "Alarm Cancelled");
            Toast.makeText(context, "Alarm Cancelled", Toast.LENGTH_SHORT).show();

//            toggleBreathe(false);
            SharedPreferences preferences = context.
                    getSharedPreferences(
                            preferencesUnitedString,
                            Context.MODE_PRIVATE
                    );
            @SuppressLint("CommitPrefEdits") final SharedPreferences.Editor preferenceEditor = preferences.edit();
            if (preferences.contains(breatheTogglePreferenceString)) {
                preferenceEditor.putBoolean(breatheTogglePreferenceString, true);
                preferenceEditor.apply();
            }

        } else {
            Log.d(TAG, "Alarm not cancelled : null checks were false");
            Toast.makeText(context, "Alarm not cancelled : null checks were false", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotification(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(preferencesUnitedString, Context.MODE_PRIVATE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationBreatheChannelIDString)
                .setSmallIcon(R.drawable.ic_lungs)
                .setContentTitle("Breathe Consciously")
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.breathe))
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, MainActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK),
                                0
                        )
                );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);

        if (preferences.getBoolean(autoCancelPreferenceString, true))
            builder.setTimeoutAfter(notificationTimeOut);

        NotificationManagerCompat notifier = NotificationManagerCompat.from(context);
        notifier.notify(0, builder.build());
    }
}
