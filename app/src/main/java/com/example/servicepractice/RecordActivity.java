package com.example.servicepractice;

import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.servicepractice.GlobalConfig.AUDIO_FORMAT;
import static com.example.servicepractice.GlobalConfig.CHANNEL_CONFIG;
import static com.example.servicepractice.GlobalConfig.SAMPLE_RATE_INHZ;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener{

    /*原本輸出是pcm不能用撥放器撥 所以要先轉檔*/
    private AudioRecord audioRecord=null;
    private AudioTrack audioTrack;
    private int recordBuffSize=0;
    private boolean isRecord=false;
    private FileInputStream fileInputStream;
    private Button mBtnConvert;
    private Button record;
    private Button mBtnControl;
    private Button mBtnPlay;
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
    }


    public void CreateAudioRecord(){
        //frequency 採樣機率 聲道 編碼
        /*最小size*/
        recordBuffSize=AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
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

    public void StopAudioRecord(){

        /*flag變false audioRecord停止並釋放 初始化*/
        isRecord=false;
        audioRecord.stop();
        audioRecord.release();
        audioRecord=null;


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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_control:
                Button button = (Button) view;
                if (button.getText().toString().equals("錄音")) {
                    button.setText("停止");
                    CreateAudioRecord();
                } else {
                    button.setText("錄音");
                    StopAudioRecord();
                }

                break;
            case R.id.btn_convert:
                PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
                File pcmFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
                File wavFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.wav");
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

    private void stopPlay() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }

    }

    private void playInModeStream() {
        /*
         * SAMPLE_RATE_INHZ 对应pcm音频的采样率
         * channelConfig 对应pcm音频的声道
         * AUDIO_FORMAT 对应pcm音频的格式
         * */
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

        File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "test.pcm");
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
}

