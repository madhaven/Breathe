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

import java.util.Calendar;
import java.util.Objects;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.example.breathe.MainActivity.alarmIntentRequestCode;
import static com.example.breathe.MainActivity.alarmStopNotificationIntentRequestCode;
import static com.example.breathe.MainActivity.interval;
import static com.example.breathe.MainActivity.notificationTimeOut;
import static com.example.breathe.MainActivity.scheduleAlarm;
import static com.example.breathe.MainActivity.scheduleAlarmStop;

public class AlarmReceiver extends BroadcastReceiver {

    public boolean
            rightNow = true,
            later = false,
            breatheToggle, autoCancel;
    private AlarmManager breatheAlarm;
    public String autoCancelPreferenceString,
            TAG,
            intervalPreferenceString,
            breatheTogglePreferenceString,
            notificationBreatheChannelIDString,
            preferencesUnitedString,
            stopPreferenceString,
            startPreferenceString;
    public int start, stop, interval;

    @Override
    public void onReceive(Context context, Intent intent) {

        autoCancelPreferenceString = context.getString(R.string.autoCancelPreferenceString);
        TAG = "madhaven";
        breatheTogglePreferenceString = context.getString(R.string.breatheTogglePreferenceString);
        notificationBreatheChannelIDString = context.getString(R.string.notificationBreatheChannelIDString);
        preferencesUnitedString = context.getString(R.string.preferencesUnitedString);
        intervalPreferenceString = context.getString(R.string.intervalPreferenceString);
        startPreferenceString = context.getString(R.string.startPreferenceString);
        stopPreferenceString = context.getString(R.string.stopPreferenceString);

        Log.d(TAG, "BR: Broadcast Received");
        breatheAlarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String action_xx = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action_xx)
                || Intent.ACTION_REBOOT.equals(action_xx)
                || Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action_xx)){
            try{

                Log.d(TAG, "BR: received broadcast : bootup sequence");
                //device powered on. initialize values and schedulers

                // obtain shared preferences and set UI values
                SharedPreferences preferences = context.
                        getSharedPreferences(
                                preferencesUnitedString,
                                Context.MODE_PRIVATE
                        );
                @SuppressLint("CommitPrefEdits") final SharedPreferences.Editor preferenceEditor = preferences.edit();
                if (preferences.contains(intervalPreferenceString))
                    interval = preferences.getInt(intervalPreferenceString, 5);
                if (preferences.contains(startPreferenceString))
                    start = preferences.getInt(startPreferenceString, 6);
                if (preferences.contains(stopPreferenceString))
                    stop = preferences.getInt(stopPreferenceString, 18);
                if (preferences.contains(autoCancelPreferenceString))
                    autoCancel = preferences.getBoolean(autoCancelPreferenceString, true);
                if (preferences.contains(breatheTogglePreferenceString))
                    breatheToggle = preferences.getBoolean(breatheTogglePreferenceString, false);

                scheduleAlarm(start, context, breatheAlarm);
                scheduleAlarmStop(stop, context, breatheAlarm);
                Log.d(TAG, "BR: start : "+ start+"stop : "+stop+", calHour : "+Calendar.HOUR_OF_DAY);
                int curHour = Calendar.HOUR_OF_DAY;
                if (
                        (start <= curHour && curHour < stop)|| //sta c sto
                        (start <= curHour && stop <= start)|| //sto sta c
                        (curHour<stop && stop<=start)){ //c sto sta
                    scheduleAlarm(-1, context, breatheAlarm);
                }

            } catch (Exception e) {
                Log.d(TAG, e.getStackTrace()[0].getClassName()+" "+e.getStackTrace()[0].getLineNumber()+" "+Objects.requireNonNull(e.getLocalizedMessage()));
            }

        } else {
            String intention = intent.getStringExtra("Intention");
            if (intention == null) intention = "nothing";
            switch (intention) {
                case "startAlarm":
                    Log.d(TAG, "BR: starting alarm");
                    startAlarms(context);
                    break;
                case "stopAlarm":
                    Log.d(TAG, "BR: stopping alarm");
                    stopAlarms(context);
                    break;
                default:
                    Log.d(TAG, "BR: sending notification");
                    sendNotification(context);
            }
        }
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
            Log.d(TAG, "BR: Alarm Cancelled");
            Toast.makeText(context, "Alarm Cancelled", Toast.LENGTH_SHORT).show();

//            toggleBreathe(false);
            SharedPreferences preferences = context.
                    getSharedPreferences(
                            preferencesUnitedString,
                            Context.MODE_PRIVATE
                    );
            @SuppressLint("CommitPrefEdits") final SharedPreferences.Editor preferenceEditor = preferences.edit();
            if (preferences.contains(breatheTogglePreferenceString)) {
                preferenceEditor.putBoolean(breatheTogglePreferenceString, false);
                preferenceEditor.apply();
            } else {
                Log.d(TAG, "BR: stopAlarms: cannot edit preferences");
            }

        } else {
            Log.d(TAG, "BR: Alarm not cancelled : null checks were false");
        }
    }

    private void sendNotification(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(preferencesUnitedString, Context.MODE_PRIVATE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationBreatheChannelIDString)
                .setSmallIcon(R.drawable.ic_lungs)
                .setContentTitle("Breathe Consciously")
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + R.raw.breathe))
                .addAction(
                        R.drawable.ic_delete_black_24dp,
                        "Stop Reminders",
                        PendingIntent.getBroadcast(
                                context,
                                alarmStopNotificationIntentRequestCode,
                                new Intent(context, AlarmReceiver.class)
                                        .putExtra("Intention", "stopAlarm"),
                                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT
                        ))
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
