package com.example.breathe;

import androidx.annotation.LongDef;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private SeekBar intervalSeek, startSeek, stopSeek;
    private TextView intervalText, startText, stopText;
    private ToggleButton breatheToggle;
    private Switch autoCancelSwitch;

    public int interval, start, stop;
    public static String TAG = "madhaven";

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

        intervalSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                playTick(seekBar);
                interval = (i == 0) ? 1 : i * 5;
                Log.d(TAG, "interval : " + interval);
                intervalText.setText(String.valueOf(interval));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        startSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                playTick(seekBar);
                start = i;
                Log.d(TAG, "start : " + start);
                startText.setText(String.valueOf(start));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        stopSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                playTick(seekBar);
                stop = i;
                Log.d(TAG, "stop : " + stop);
                stopText.setText(String.valueOf(stop));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void playTick(View view) {
//        view.playSoundEffect(Resource);
    }

}
