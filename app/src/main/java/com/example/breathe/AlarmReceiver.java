package com.example.breathe;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.example.breathe.MainActivity.TAG;
import static com.example.breathe.MainActivity.notificationAutoCancel;
import static com.example.breathe.MainActivity.notificationBreatheChannelID;
import static com.example.breathe.MainActivity.notificationTimeOut;
import static com.example.breathe.MainActivity.testIntervalCount;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String theMessage = "BroadCast Received : "+ String.valueOf(++testIntervalCount);
        Log.d(TAG, theMessage);
        sendNotification(context);
//        Toast.makeText(context, theMessage, Toast.LENGTH_SHORT).show();
        // TODO: send notification with variables from main
    }

    private void sendNotification(Context context){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationBreatheChannelID)
                .setSmallIcon(R.drawable.ic_lungs)
                .setContentTitle("Breathe Consciously")
//                .setContentText("you want this here ?")
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"+context.getPackageName()+"/"+R.raw.breathe))
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                0,
                                new Intent(context, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK),
                                0
                        )
                );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);

        if (notificationAutoCancel)
            builder.setTimeoutAfter(notificationTimeOut);

        NotificationManagerCompat notifier = NotificationManagerCompat.from(context);
        notifier.notify(0, builder.build());
    }
}
