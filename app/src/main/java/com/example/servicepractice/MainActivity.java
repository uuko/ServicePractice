package com.example.servicepractice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.example.servicepractice.service.LongRunningService;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{
    Intent intent;
    private boolean status = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button service=findViewById(R.id.service);
        Button AudioRecord=findViewById(R.id.button3);
        Button notifi=findViewById(R.id.notifi);
        notifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent notifi=new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(notifi);
            }
        });
        AudioRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent AudioRecord=new Intent(MainActivity.this,RecordActivity.class);
                startActivity(AudioRecord);
            }
        });
        service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status){
                    service.setText("停止");
                    Intent stopIntent = new Intent(MainActivity.this, LongRunningService.class);
                    stopService(stopIntent);
                    status=false;
                }else {
                    service.setText("開始");
                    intent = new Intent(MainActivity.this, LongRunningService.class);
//                    PendingIntent pintent = PendingIntent.getService(MainActivity.this, 0, intent, 0);
//                    AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//                    int anHour = 3* 1000;
//        /*這個是開機的時間*/
//                    long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
//                    alarm.set(AlarmManager.RTC_WAKEUP,triggerAtTime , pintent);
                    startService(intent);
                    status=true;
                }
            }
        });
        Button recorder=findViewById(R.id.recorder);
        recorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent re =new Intent(MainActivity.this,RecorderActivity.class);
                startActivity(re);
            }
        });

        Button button=findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        startAlarm(c);
    }

    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        int alarmId = SharedPreUtils.getInt(this, "alarm_id", 0);
        Log.d("fxxk", "startAlarm: "+alarmId);
        SharedPreUtils.setInt(this, "alarm_id", ++alarmId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmId, intent, 0);

        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }
}
