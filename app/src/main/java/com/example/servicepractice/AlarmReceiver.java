package com.example.servicepractice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.servicepractice.service.LongRunningService;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private  NotificationManager notificationManager;
    private int NOTIFICATION_ID=0;
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, LongRunningService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager=
                    (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel=
                    new NotificationChannel(PRIMARY_CHANNEL_ID,
                            "123" ,NotificationManager.IMPORTANCE_HIGH
                    );
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("hihi");
            notificationManager.createNotificationChannel(notificationChannel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("My notification")
                    .setContentText("Much longer text that cannot fit one line...")
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("Much longer text that cannot fit one line..."))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NOTIFICATION_ID++;
            notificationManager.notify(NOTIFICATION_ID,builder.build());

            ContextCompat.startForegroundService(context,i);
        //context.startService(i);
           // ContextCompat.startForegroundService(context,i);
            //限制五秒
        } else {
//            notificationManager=
//                    (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
//                    .setSmallIcon(R.drawable.ic_launcher_background)
//                    .setContentTitle("My notification")
//                    .setContentText("Much longer text that cannot fit one line...")
//                    .setStyle(new NotificationCompat.BigTextStyle()
//                            .bigText("Much longer text that cannot fit one line..."))
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//            NOTIFICATION_ID++;
//            notificationManager.notify(NOTIFICATION_ID,builder.build());
            context.startService(i);
        }
    }


}
