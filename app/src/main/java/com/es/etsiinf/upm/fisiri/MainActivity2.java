package com.es.etsiinf.upm.fisiri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity2 extends AppCompatActivity {
    public final static int REQUEST_RECORD_AUDIO = 2033;
    protected TextView text1;
    protected TextView text2 ;
    protected Button recordButton ;
    protected Button stopbutton ;
    protected TextView specsT;
    protected TextView outputT;


    private AudioRecord audioRecord;
    private TimerTask timerTask;
    private AudioClassifier audioClassifier;
    private TensorAudio tensorAudio;

    float probabilityThreshold = 0.3f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        //UI parts
          text1= findViewById(R.id.title_record);
          text2 = findViewById(R.id.title_act);
          recordButton = findViewById(R.id.buttonrecord);
          stopbutton = findViewById(R.id.buttonstop);
          specsT = findViewById(R.id.specstextview);
          outputT = findViewById(R.id.outputtextview);
          stopbutton.setEnabled(false);



        // Solicitar permiso para grabar audiom
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }

        Animation fadeinan = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        text1.startAnimation(fadeinan);
        text2.startAnimation(fadeinan);

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


    public void onStartRecording(View view) {
        recordButton.setVisibility(View.INVISIBLE);
        stopbutton.setVisibility(View.VISIBLE);
        recordButton.setEnabled(false);
        stopbutton.setEnabled(true);
        // Loading the model from the assets folder
        try {
            String modelPath = "soundclassifier_with_metadata.tflite";
            audioClassifier = AudioClassifier.createFromFile(this, modelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Creating an audio recorder
        tensorAudio = audioClassifier.createInputTensorAudio();


        // showing the audio recorder specification
        TensorAudio.TensorAudioFormat format = audioClassifier.getRequiredTensorAudioFormat();
        String specs = "Number of channels: " + format.getChannels() + "\n"
                + "Sample Rate: " + format.getSampleRate();
        specsT.setText(specs);


        // Creating and start recording
        audioRecord = audioClassifier.createAudioRecord();
        audioRecord.startRecording();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                // Classifying audio data
                // val numberOfSamples = tensor.load(record)
                // val output = classifier.classify(tensor)
                int numberOfSamples = tensorAudio.load(audioRecord);
                List<Classifications> output = audioClassifier.classify(tensorAudio);

                // Filtering out classifications with low probability
                List<Category> finalOutput = new ArrayList<>();
                for (Classifications classifications : output) {
                    for (Category category : classifications.getCategories()) {
                        if (category.getScore() > probabilityThreshold) {
                            finalOutput.add(category);
                        }
                    }
                }

                // Sorting the results
                Collections.sort(finalOutput, (o1, o2) -> (int) (o1.getScore() - o2.getScore()));

                // Creating a multiline string with the filtered results
                StringBuilder outputStr = new StringBuilder();
                for (Category category : finalOutput) {
                    outputStr.append(category.getLabel())
                            .append(": ").append(category.getScore()).append("\n");
                }

                // Updating the UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalOutput.isEmpty()) {
                            outputT.setText("Could not classify");
                        } else {
                            outputT.setText(outputStr.toString());
                        }
                    }
                });
            }
        };

        new Timer().scheduleAtFixedRate(timerTask, 1, 500);
    }

    public void onStopRecording(View view) {
        recordButton.setVisibility(View.VISIBLE);
        stopbutton.setVisibility(View.INVISIBLE);

        recordButton.setEnabled(true);
        stopbutton.setEnabled(false);
        timerTask.cancel();
        audioRecord.stop();
    }
}