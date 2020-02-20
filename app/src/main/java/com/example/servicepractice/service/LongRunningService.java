package com.example.servicepractice.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.servicepractice.AlarmReceiver;
import com.example.servicepractice.R;

import java.util.Date;

public class LongRunningService extends Service {
    /*沒有remote的話 他還是在主線呈喔 時間太長會anr*/
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private  NotificationManager notificationManager;
    private int NOTIFICATION_ID=0;
    /*可以用綁的 特定activity 但是service都可以綁到每個act*/
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    /*執行的時候必跑 其他都跑oncreate*/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("LongRunningService", "executed at " + new Date().
                        toString());
                AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
                /*加remote就可以多很多秒XD*/
                int anHour = 10* 1000;
                /*這個是開機的時間*/
                long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
                Intent i = new Intent(getApplicationContext(), AlarmReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, i, 0);
                manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
//                            notificationManager=
//                    (NotificationManager)getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
//            NotificationChannel notificationChannel=
//                    new NotificationChannel(PRIMARY_CHANNEL_ID,
//                            "123" ,NotificationManager.IMPORTANCE_HIGH
//                    );
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.RED);
//            notificationChannel.enableVibration(true);
//            notificationChannel.setDescription("hihi");
//            notificationManager.createNotificationChannel(notificationChannel);
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), PRIMARY_CHANNEL_ID)
//                    .setSmallIcon(R.drawable.ic_launcher_background)
//                    .setContentTitle("My notification")
//                    .setContentText("Much longer text that cannot fit one line...")
//                    .setStyle(new NotificationCompat.BigTextStyle()
//                            .bigText("Much longer text that cannot fit one line..."))
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//            NOTIFICATION_ID++;
//            notificationManager.notify(NOTIFICATION_ID,builder.build());
            }
        }).start();


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        Intent i = new Intent(this, AlarmReceiver.class);
//        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
//        manager.cancel(pi);
    }
}
