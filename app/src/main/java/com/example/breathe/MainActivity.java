package com.example.breathe;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.AlarmManagerCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    //UI components
    private TextView intervalText, startText, stopText;
    private ToggleButton breatheToggleButton;
    private AlarmManager breatheAlarm;
    private Vibrator vibrator;
    private SharedPreferences preferences;
    MediaPlayer tickMediaPlayer;

    //App Logic
    public static int interval, start, stop;
    public boolean breatheToggle;
    public static boolean autoCancel;
    public static int notificationTimeOut = 10000;

    //Keys
    private static String TAG = "madhaven";
    private String
            preferencesUnitedString,
            intervalPreferenceString,
            breatheTogglePreferenceString,
            autoCancelPreferenceString,
            startPreferenceString,
            stopPreferenceString,
            notificationBreatheChannelIDString;
    public static int
            alarmIntentRequestCode = 456,
            alarmStartIntentRequestCode = 898,
            alarmStopIntentRequestCode = 231,
            alarmStopNotificationIntentRequestCode = 98721;
    public boolean
            rightNow = true,
            later = false;


    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        tickMediaPlayer = MediaPlayer.create(this, R.raw.tickk);

        // obtain shared preferences and set UI values
        preferencesUnitedString = getString(R.string.preferencesUnitedString);
        intervalPreferenceString = getString(R.string.intervalPreferenceString);
        breatheTogglePreferenceString = getString(R.string.breatheTogglePreferenceString);
        autoCancelPreferenceString = getString(R.string.autoCancelPreferenceString);
        startPreferenceString = getString(R.string.startPreferenceString);
        stopPreferenceString = getString(R.string.stopPreferenceString);
        notificationBreatheChannelIDString = getString(R.string.notificationBreatheChannelIDString);
        preferences = getApplication().
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
        createNotificationChannel(this.getApplicationContext());

        setContentView(R.layout.activity_main);
        SeekBar intervalSeek = findViewById(R.id.interval_seek);
        SeekBar startSeek = findViewById(R.id.start_seek);
        SeekBar stopSeek = findViewById(R.id.stop_seek);
        intervalText = findViewById(R.id.interval_text);
        startText = findViewById(R.id.start_text);
        stopText = findViewById(R.id.stop_text);
        breatheToggleButton = findViewById(R.id.breatheToggle);
        Switch autoCancelSwitch = findViewById(R.id.auto_cancel_switch);
        breatheAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        intervalSeek.setProgress(interval/5);
        intervalText.setText(String.valueOf(interval));
        startSeek.setProgress(start);
        startText.setText(String.valueOf(start));
        stopSeek.setProgress(stop);
        stopText.setText(String.valueOf(stop));
        autoCancelSwitch.setChecked(autoCancel);
        breatheToggleButton.setChecked(breatheToggle);

        breatheToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // handles whether or not the reminders are active.
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferenceEditor.putBoolean(breatheTogglePreferenceString, b);
                preferenceEditor.apply();
                breatheToggle = b;
                if (breatheToggle) scheduleAlarm(-1, getApplicationContext(), breatheAlarm);
                else scheduleAlarmStop(-1, getApplicationContext(), breatheAlarm);
                playTick();
                Log.d(TAG, "MA: breathtoggle : " + breatheToggle);
            }
        });

        autoCancelSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // handles variables that decided whether or not to let notifications persist
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                autoCancel = b;
                preferenceEditor.putBoolean(autoCancelPreferenceString, b);
                preferenceEditor.apply();
                playTick();
                Log.d(TAG, "MA: notification : " + autoCancel);
            }
        });

        intervalSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // decides when to start the notifications in a day
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                playTick();
                interval = (i == 0) ? 1 : (i * 5);
                intervalText.setText(String.valueOf(interval));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferenceEditor.putInt(intervalPreferenceString, interval);
                preferenceEditor.apply();
                breatheToggleButton.setChecked(false);
                Log.d(TAG, "MA: interval : " + interval);
            }
        });

        startSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // decides when to stop the notifications in a day
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                playTick();
                start = i;
                startText.setText(String.valueOf(start));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferenceEditor.putInt(startPreferenceString, start);
                preferenceEditor.apply();
                scheduleAlarm(start, getApplicationContext(), breatheAlarm);
            }
        });

        stopSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // updates the text to show stop time
                playTick();
                stop = i;
                stopText.setText(String.valueOf(stop));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferenceEditor.putInt(stopPreferenceString, stop);
                preferenceEditor.apply();
                scheduleAlarmStop(stop, getApplicationContext(), breatheAlarm);
            }
        });

    }

    private void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    notificationBreatheChannelIDString,
                    getString(R.string.breathe_reminder),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setSound(
                    Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.breathe),
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
            );
            String description = getString(R.string.channel_description);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void playTick() {

        if (vibrator != null) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createOneShot(
                        500,
                        VibrationEffect.EFFECT_TICK
                ));
            } else {
                vibrator.vibrate(50);
            }
            // plays the ticking sounds when the sliders are in progress
            if (tickMediaPlayer.isLooping())
                tickMediaPlayer.stop();
            tickMediaPlayer.start();
        }
    }

    public static void scheduleAlarm(int time, Context context, AlarmManager breatheAlarm) {
        if (time==-1){
            Log.d(TAG, "MA: scheduleAlarm immediately");
            context.sendBroadcast(
                    new Intent(context, AlarmReceiver.class)
                            .putExtra("Intention", "startAlarm")
            );
        } else {

            Log.d(TAG, "MA: scheduleAlarm at : "+time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis() - 1);
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE)+1);
            calendar.set(Calendar.HOUR_OF_DAY, time);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Log.d(TAG, "MA: scheduleAlarm: "+calendar.get(Calendar.HOUR_OF_DAY)+" "+calendar.get(Calendar.MINUTE)+" "+calendar.get(Calendar.SECOND));

            breatheAlarm.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    24 * 60 * 60 * 1000,
//                System.currentTimeMillis(),
//                3/2 *60*1000, //testing with 1.5 minute intervals
                    PendingIntent.getBroadcast(
                            context,
                            alarmStartIntentRequestCode,
                            new Intent(context, AlarmReceiver.class)
                                    .putExtra("Intention", "startAlarm"),
                            PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT
                    )
            );
        }
    }

    public static void scheduleAlarmStop(int time, Context context, AlarmManager breatheAlarm) {
        if (time==-1){
            Log.d(TAG, "MA: scheduleAlarmStop immediately");
            context.sendBroadcast(
                    new Intent(context, AlarmReceiver.class)
                            .putExtra("Intention", "stopAlarm")
            );
        } else {

            Log.d(TAG, "MA: scheduleAlarmStop at : "+time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis() - 1);
            calendar.set(Calendar.HOUR_OF_DAY, time);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            breatheAlarm.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    24 * 60 * 60 * 1000,
//                System.currentTimeMillis()+90*1000,
//                3/2 *60*1000, //testing with 1.5 minute intervals
                    PendingIntent.getBroadcast(
                            context,
                            alarmStopIntentRequestCode,
                            new Intent(context, AlarmReceiver.class)
                                    .putExtra("Intention", "stopAlarm"),
                            PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_UPDATE_CURRENT
                    )
            );
        }
    }

}
