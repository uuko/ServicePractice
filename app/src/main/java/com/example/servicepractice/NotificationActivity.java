package com.example.servicepractice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NotificationActivity extends AppCompatActivity {
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager notificationManager;
    private static final int NOTIFICATION_ID = 0;
    private static final String ACTION_UPDATE_NOTIFICATION =
            "com.example.android.Notifaction.ACTION_UPDATE_NOTIFICATION";
    private NotificationReciever notificationReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        notificationReciever=new NotificationReciever();
        Button update=(Button) findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNotification();
            }
        });
        Button cancel=(Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelupdateNotification();
            }
        });
        Button notify=(Button) findViewById(R.id.notify);
        notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification();
            }
        });
        createNotificationChannel();
        setbuttonstate(true,false,false);
        registerReceiver(notificationReciever,new IntentFilter(ACTION_UPDATE_NOTIFICATION));
    }

    @Override
    public  void onDestroy(){
        unregisterReceiver(notificationReciever);
        super.onDestroy();
    }

    public  void updateNotification(){
        Bitmap bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.mascot_1);
        NotificationCompat.Builder notifybuilder=getNotificationBuilder();
        notifybuilder.setStyle(new NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap)
                .setBigContentTitle("bigggg"));
        notificationManager.notify(NOTIFICATION_ID,notifybuilder.build());
        setbuttonstate(false,true,true);
    }

    public  void  cancelupdateNotification(){
        notificationManager.cancel(NOTIFICATION_ID);
    }

    public void sendNotification(){
        Intent intent=new Intent(ACTION_UPDATE_NOTIFICATION);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this,NOTIFICATION_ID,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
        notifyBuilder.addAction(R.drawable.ic_update,"update",pendingIntent);
        notificationManager.notify(NOTIFICATION_ID, notifyBuilder.build());
        setbuttonstate(false,true,true);
    }

    void setbuttonstate(Boolean isNotifyEnabled,Boolean isUpdadateEnabled,Boolean isCancelEnabled){
        Button update=(Button) findViewById(R.id.update);
        Button notify=(Button) findViewById(R.id.notify);
        Button cancel=(Button) findViewById(R.id.cancel);
        notify.setEnabled(isNotifyEnabled);
        update.setEnabled(isUpdadateEnabled);
        cancel.setEnabled(isCancelEnabled);
    }

    public void   createNotificationChannel(){
        notificationManager=
                    (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.O){
                /*優先程度*/
                NotificationChannel notificationChannel=
                        new NotificationChannel(PRIMARY_CHANNEL_ID,
                                "123" ,NotificationManager.IMPORTANCE_HIGH
                        );
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setDescription("hihi");
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private NotificationCompat.Builder getNotificationBuilder(){
        Intent notifyIntent=new Intent(this,MainActivity.class);
        PendingIntent notifyPendingIntent=PendingIntent.getActivity(this,
                NOTIFICATION_ID,notifyIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notifyBuilder=
                new NotificationCompat.Builder(this,PRIMARY_CHANNEL_ID)
                        .setContentTitle("notify~~~")
                        .setContentText("uuko notify!!")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentIntent(notifyPendingIntent)
                        .setAutoCancel(true);
        return notifyBuilder;
    }

    public class  NotificationReciever extends BroadcastReceiver {
        public NotificationReciever(){
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotification();
        }
    }
}
