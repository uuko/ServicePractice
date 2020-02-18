package com.example.servicepractice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecorderActivity extends AppCompatActivity  implements View.OnClickListener {
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private List<String> mPermissionList = new ArrayList<>();
    private MediaPlayer mMediaPlayer;

    private MediaRecorder mMediaRecorder;

    private File audioFile;
    private static final int MY_PERMISSIONS_REQUEST = 1000;
   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
       Button startRecorder = findViewById(R.id.startRecorder);
       startRecorder.setOnClickListener(this);

       Button stopRecorder = findViewById(R.id.stopRecorder);
       stopRecorder.setOnClickListener(this);


       Button startPlay = findViewById(R.id.startPlay);
       startPlay.setOnClickListener(this);

       Button stopPlay = findViewById(R.id.stopPlay);
       stopPlay.setOnClickListener(this);
       mMediaPlayer = new MediaPlayer();
       mMediaRecorder = new MediaRecorder();
       checkPermissions();
       /*記得要權限不然會報錯*/
       if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
               != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO },
                   1000);
       } else {
           // mic 錄音
           mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
           // 輸出格式
           mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);

           // 編碼格式
           mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
       }




       // 目錄
       File sdcard = Environment.getExternalStorageDirectory();

       audioFile = new File(sdcard, "zhangphil.amr");
       try {
           audioFile.createNewFile();
       } catch (IOException e) {
           e.printStackTrace();
       }

   }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) !=
                        PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (!mPermissionList.isEmpty()) {
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
            }
        }
    }

    private void startRecorder() throws Exception {
        mMediaRecorder.setOutputFile(audioFile.getAbsolutePath());
        mMediaRecorder.prepare();
        mMediaRecorder.start();
    }

    private void stopRecorder() {
        mMediaRecorder.stop();
        mMediaRecorder.release();
    }

    private void startPlay() throws Exception {
        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(audioFile.getAbsolutePath());

//         //靜態文件
//         mMediaPlayer.create(this, R.raw.xxx);

        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            // 循環撥放
            // mMediaPlayer.setLooping(true);
        } else {
            mMediaPlayer.pause();
        }
    }

    // 停止播放
    private void stopPlay() {
        mMediaPlayer.stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startRecorder:
                try {
                    startRecorder();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.stopRecorder:
                stopRecorder();
                break;

            case R.id.startPlay:
                try {
                    startPlay();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.stopPlay:
                stopPlay();
                break;
        }
    }

    // 在destroy摧毀資源
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }

        // 释放资源
        mMediaPlayer.release();
        mMediaRecorder.release();
    }

}
