package com.es.etsiinf.upm.fisiri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.Manifest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity2 extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int SAMPLE_RATE = 16000;
    private static final int RECORDING_LENGTH = SAMPLE_RATE * 2; // 2 seconds buffer

    private ByteBuffer audioBuffer;
    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private ProgressBar progressBar;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        final TextView text1= findViewById(R.id.title_record);
        final TextView text2 = findViewById(R.id.title_act);


        Animation fadeinan = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        text1.startAnimation(fadeinan);
        text2.startAnimation(fadeinan);


        // Solicitar permiso para grabar audiom
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        progressBar = findViewById(R.id.progress_bar);
        handler = new Handler(Looper.getMainLooper());

        audioBuffer = ByteBuffer.allocateDirect(RECORDING_LENGTH * 2); // 16-bit PCM
        audioBuffer.order(ByteOrder.nativeOrder());

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                audioBuffer.capacity()
        );

        final Button botonrecord = findViewById(R.id.button2);
        botonrecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRec();
            }
        });

        fadeinan.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                text1.setVisibility(View.VISIBLE);
                text2.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }


        });



    }
    private void startRec() {
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        audioRecord.startRecording();
        isRecording = true;
        audioBuffer.clear();

        new Thread(() -> {
            int result;
            int progress = 0;
            result = audioRecord.read(audioBuffer, audioBuffer.capacity());
                /*if (result > 0) {
                    processAudio();
                }*/
            while (progress <= 100 && isRecording) {

                progress++;
                final int currentProgress = progress;
                handler.post(() -> progressBar.setProgress(currentProgress));
                try {
                    Thread.sleep(20); // Esperar 20ms antes de actualizar el progreso
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            stopRecording();
            handler.post(() -> progressBar.setVisibility(View.GONE)); // Ocultar ProgressBar al finalizar
        }).start();
    }

    private void stopRecording() {
        if (isRecording) {
            audioRecord.stop();
            isRecording = false;
            handler.post(() -> progressBar.setVisibility(View.GONE));
        }
    }
}