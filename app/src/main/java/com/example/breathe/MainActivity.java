package com.example.breathe;

import androidx.annotation.LongDef;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.AlarmManagerCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private SeekBar intervalSeek, startSeek, stopSeek;
    private TextView intervalText, startText, stopText;
    private ToggleButton breatheToggle;
    private Switch autoCancelSwitch;
    public AlarmManager breatheAlarm;
    public PendingIntent alarmIntent;

    public int interval, start, stop;
    public boolean breatheToggled;
    public boolean notificationToggled;
    public static String TAG = "madhaven";

    public int alarmIntentRequestCode = 456;
    public static int testIntervalCount = 0;
    public static String notificationBreatheID = "com.Breathe.NotificationBreatheID";

    @SuppressLint("ServiceCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intervalSeek = findViewById(R.id.interval_seek);
        startSeek = findViewById(R.id.start_seek);
        stopSeek = findViewById(R.id.stop_seek);
        intervalText = findViewById(R.id.interval_text);
        startText = findViewById(R.id.start_text);
        stopText = findViewById(R.id.stop_text);
        breatheToggle = findViewById(R.id.breatheToggle);
        autoCancelSwitch = findViewById(R.id.auto_cancel_switch);
        createNotificationChannel();

        breatheAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarmIntent = PendingIntent.getBroadcast(
//                this, alarmIntentRequestCode
//                , new Intent(this, AlarmReceiver.class)
//                , PendingIntent.FLAG_UPDATE_CURRENT
//        );

        breatheToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // handles whether or not the reminders are active.
                breatheToggled = b;
                Log.d(TAG, "breathtoggle : " + breatheToggled);
                if (breatheToggled){
                    breatheAlarm.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            0,
                            interval * AlarmManager.INTERVAL_FIFTEEN_MINUTES/15,
                            PendingIntent.getBroadcast(
                                    getApplicationContext(),
                                    alarmIntentRequestCode,
                                    new Intent(getApplicationContext(), AlarmReceiver.class),
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            )
                    );
                } else {
                    alarmIntent = PendingIntent.getBroadcast(
                            getApplicationContext(),
                            alarmIntentRequestCode,
                            new Intent(getApplicationContext(), AlarmReceiver.class),
                            PendingIntent.FLAG_CANCEL_CURRENT
                    );
                    if (breatheAlarm!=null && alarmIntent!=null){
                        breatheAlarm.cancel(alarmIntent);
                        Log.d(TAG, "Alarm Cancelled");
                    } else {
                        Log.d(TAG, "Alarm not cancelled : null checks were false");
                    }
                }
            }
        });

        autoCancelSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // handles variables that decided whether or not to let notifications persist
                notificationToggled = b;
                Log.d(TAG, "notification : "+notificationToggled);
                // TODO: edit notification stuff
            }
        });

        intervalSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // updates text to show the interval
                playTick(seekBar);
                interval = (i == 0) ? 1 : i * 5;
                Log.d(TAG, "interval : " + interval);
                intervalText.setText(String.valueOf(interval));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO: edit alarmmanager variables
            }
        });

        startSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // updates the text to show start time
                playTick(seekBar);
                start = i;
                Log.d(TAG, "start : " + start);
                startText.setText(String.valueOf(start));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO: edit alarmmanager vairables
            }
        });

        stopSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // updates the text to show stop time
                playTick(seekBar);
                stop = i;
                Log.d(TAG, "stop : " + stop);
                stopText.setText(String.valueOf(stop));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO: edit alarm manager variables
            }
        });

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    notificationBreatheID,
                    getString(R.string.breathe_reminder),
                    NotificationManager.IMPORTANCE_LOW
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

}
