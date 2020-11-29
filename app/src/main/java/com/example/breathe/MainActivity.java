package com.example.breathe;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    //UI components
    private TextView intervalText, startText, stopText;
    private ToggleButton breatheToggleButton;
    public static AlarmManager breatheAlarm;

    //App Logic
    public static int interval, start, stop;
    public boolean breatheToggle;
    public static boolean autoCancel;
    public static int notificationTimeOut = 10000;

    //Keys
    public static String
            TAG = "madhaven",
            preferencesUnitedString = "the United string of preference",
            intervalPreferenceString = "intervalString",
            breatheTogglePreferenceString = "breatheToggleString",
            autoCancelPreferenceString = "notificationAutoCancelString",
            startPreferenceString = "startString",
            stopPreferenceString = "stopString",
            notificationBreatheChannelIDString = "com.Breathe.NotificationBreatheID";
    public static int
            alarmIntentRequestCode = 456,
            alarmStartIntentRequestCode = 898,
            alarmStopIntentRequestCode = 231;

    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
//        if (Build.VERSION.SDK_INT < 26)
            android.os.SystemClock.sleep(2000);
        createNotificationChannel(this);

        // obtain shared preferences and set UI values
        SharedPreferences preferences = getApplication().
                getSharedPreferences(
                        preferencesUnitedString,
                        Context.MODE_PRIVATE
                );
        @SuppressLint("CommitPrefEdits") final SharedPreferences.Editor preferenceEditor = preferences.edit();
        if (preferences.contains(intervalPreferenceString)) {
            interval = preferences.getInt(intervalPreferenceString, 5);
        }
        if (preferences.contains(startPreferenceString)) {
            start = preferences.getInt(startPreferenceString, 6);
        }
        if (preferences.contains(stopPreferenceString)) {
            stop = preferences.getInt(stopPreferenceString, 18);
        }
        if (preferences.contains(autoCancelPreferenceString)) {
            autoCancel = preferences.getBoolean(autoCancelPreferenceString, true);
        }
        if (preferences.contains(breatheTogglePreferenceString)) {
            breatheToggle = preferences.getBoolean(breatheTogglePreferenceString, false);
        }

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

        breatheToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // handles whether or not the reminders are active.
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferenceEditor.putBoolean(breatheTogglePreferenceString, b);
                preferenceEditor.apply();
                breatheToggle = b;
                if (breatheToggle) startAlarm();
                else stopAlarm();
                Log.d(TAG, "breathtoggle : " + breatheToggle);
            }
        });

        autoCancelSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // handles variables that decided whether or not to let notifications persist
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                autoCancel = b;
                preferenceEditor.putBoolean(autoCancelPreferenceString, b);
                preferenceEditor.apply();
                Log.d(TAG, "notification : " + autoCancel);
            }
        });

        intervalSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // decides when to start the notifications in a day
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                playTick(seekBar);
                interval = (i == 0) ? 1 : i * 5;
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
                Log.d(TAG, "interval : " + interval);
            }
        });

        startSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // decides when to stop the notifications in a day
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                playTick(seekBar);
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
                startAlarm();
                Log.d(TAG, "start : " + start);
            }
        });

        stopSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // updates the text to show stop time
                playTick(seekBar);
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
                stopAlarm();
                Log.d(TAG, "stop : " + stop);
            }
        });

        intervalSeek.setProgress(interval/5);
        startSeek.setProgress(start);
        stopSeek.setProgress(stop);
        autoCancelSwitch.setChecked(autoCancel);
        breatheToggleButton.setChecked(breatheToggle);

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
                    Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.breathe),
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
            );
//            String description = getString(R.string.channel_description);
//            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void playTick(View view) {
        // plays the ticking sounds when the sliders are in progress
        ;// TODO: SOUND CODE
    }

    private void startAlarm() {
        breatheAlarm.set(
                AlarmManager.RTC_WAKEUP,
                1000,
                PendingIntent.getBroadcast(
                        getApplicationContext(),
                        alarmStartIntentRequestCode,
                        new Intent(getApplicationContext(), AlarmReceiver.class)
                                .putExtra("Intention", "startAlarm"),
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT
                )
        );
        // TODO: edit alarmmanager vairables
    }

    private void stopAlarm() {
        breatheAlarm.set(
                AlarmManager.RTC_WAKEUP,
                1000,
                PendingIntent.getBroadcast(
                        getApplicationContext(),
                        alarmStopIntentRequestCode,
                        new Intent(getApplicationContext(), AlarmReceiver.class)
                                .putExtra("Intention", "stopAlarm"),
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT
                )
        );
        // TODO: edit alarm manager variables
    }

}
