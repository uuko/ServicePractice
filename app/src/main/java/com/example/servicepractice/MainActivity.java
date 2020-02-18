package com.example.servicepractice;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Intent;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.servicepractice.service.LongRunningService;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button service=findViewById(R.id.service);
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
                intent = new Intent(MainActivity.this, LongRunningService.class);
                startService(intent);
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
    }
}
