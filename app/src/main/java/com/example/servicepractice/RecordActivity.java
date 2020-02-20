package com.example.servicepractice;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.example.servicepractice.GlobalConfig.AUDIO_FORMAT;
import static com.example.servicepractice.GlobalConfig.CHANNEL_CONFIG;
import static com.example.servicepractice.GlobalConfig.SAMPLE_RATE_INHZ;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener{


    /*
    * 跟另一個插在這個適合持續撥放不用解碼
    * AudioTrack 则更接近底层，提供了非常强大的控制能力，支持低延迟播放，适合流媒体和VoIP语音电话等场景。
    * 而且這個是底層的
    * */
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


        /*原本輸出是pcm不能用撥放器撥 所以要先轉檔*/
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private List<String> mPermissionList = new ArrayList<>();
    private boolean isRecord=false;
    private FileInputStream fileInputStream;
    private Button mBtnConvert;
    private Button record;
    private Button mBtnControl;
    private Button mBtnPlay;
    private static byte[] audioData;
    private static final int MY_PERMISSIONS_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorderr);
        mBtnControl = findViewById(R.id.btn_control);
        mBtnControl.setOnClickListener(this);
        mBtnConvert = findViewById(R.id.btn_convert);
        mBtnConvert.setOnClickListener(this);
        mBtnPlay = findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(this);


        checkPermissions();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_control:
                Button button = (Button) view;
                if (button.getText().toString().equals("錄音")) {
                    button.setText("停止");
                    startRecord();
                } else {
                    button.setText("錄音");
                    stopRecord();
                }

                break;
            case R.id.btn_convert:
                PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
                File pcmFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "ttt.pcm");
                File wavFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "ttt.wav");
                if (!wavFile.mkdirs()) {

                }
                if (wavFile.exists()) {
                    wavFile.delete();
                }
                pcmToWavUtil.pcmToWav(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath());

                break;
            case R.id.btn_play:
                Button btn = (Button) view;
                String string = btn.getText().toString();
                if (string.equals("撥放")) {
                    btn.setText("停止");
                    playInModeStream();
                    //playInModeStatic();
                } else {
                    btn.setText("撥放");
                    stopPlay();
                }
                break;

            default:
                break;
        }
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


    public void startRecord() {

        //frequency 採樣機率 聲道 編碼
        /*最小size*/
        final int recordBuffSize=AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        audioRecord=new AudioRecord(MediaRecorder.AudioSource.MIC,44100, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT
                ,recordBuffSize);
        final byte data[]=new byte[recordBuffSize];

        /*開始錄音*/
        audioRecord.startRecording();
        isRecord=true;

        /*創檔案存取位置*/
        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "ttt.pcm");
        if (!file.mkdirs()) {
            Log.e("777", "Directory not created");
        }
        if (file.exists()) {
            file.delete();
        }
        /*開一個新thread創數據*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*檔案複製的輸出輸入必須用到*/
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (null != outputStream) {
                    while (isRecord) {
                        /*
                         * public int read(byte[] audioData, int offsetInBytes, int sizeInBytes)
                         * 從緩衝區讀資料 複製到指定區域 如果audiobuffer不是緩衝區則返回0
                         * 讀的bytes不能超過sizeInBytes
                         * */
                        int read = audioRecord.read(data, 0, recordBuffSize);
                        /*沒錯就寫入*/
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                outputStream.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        Log.i("777", "run: close file output stream !");
                        /*關數據流*/
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }


    public void stopRecord() {
        /*flag變false audioRecord停止並釋放 初始化*/
        isRecord=false;
        if (null != audioRecord) {
        audioRecord.stop();
        audioRecord.release();
        audioRecord=null;}
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


    /**
     * 播放，使用stream模式
     */
    private void playInModeStream() {
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        final int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_INHZ, channelConfig, AUDIO_FORMAT);
        audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder().setSampleRate(SAMPLE_RATE_INHZ)
                        .setEncoding(AUDIO_FORMAT)
                        .setChannelMask(channelConfig)
                        .build(),
                minBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE);
        audioTrack.play();

        File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "ttt.pcm");
        try {
            fileInputStream = new FileInputStream(file);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] tempBuffer = new byte[minBufferSize];
                        while (fileInputStream.available() > 0) {
                            int readCount = fileInputStream.read(tempBuffer);
                            if (readCount == AudioTrack.ERROR_INVALID_OPERATION ||
                                    readCount == AudioTrack.ERROR_BAD_VALUE) {
                                continue;
                            }
                            if (readCount != 0 && readCount != -1) {
                                audioTrack.write(tempBuffer, 0, readCount);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 播放，使用static模式
     * 小一點的東西很適合 他是一次寫入的
     */
//    private static void playInModeStatic() {
//        // static模式，需要将音频数据一次性write到AudioTrack的内部缓冲区
//
////        new AsyncTask<Void, Void, Void>() {
////            @Override
////            protected Void doInBackground(Void... params) {
////                try {
////                    InputStream in = getResources().openRawResource(R.raw.ding);
////                    try {
////                        ByteArrayOutputStream out = new ByteArrayOutputStream();
////                        for (int b; (b = in.read()) != -1; ) {
////                            out.write(b);
////                        }
////
////                        audioData = out.toByteArray();
////                    } finally {
////                        in.close();
////                    }
////                } catch (IOException e) {
////                }
////                return null;
////            }
////        }
//    }



    /**
     * 停止播放
     */
    private void stopPlay() {
        if (audioTrack != null) {

            audioTrack.stop();

            audioTrack.release();

        }
    }


}

