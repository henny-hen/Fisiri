package com.es.etsiinf.upm.fisiri;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {
    public final static int REQUEST_RECORD_AUDIO = 2033;
    protected TextView text1;
    protected TextView text2 ;
    protected Button recordButton ;
    protected Button stopbutton ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

          text1= findViewById(R.id.title_record);
          text2 = findViewById(R.id.title_act);
          recordButton = findViewById(R.id.buttonrecord);
          stopbutton = findViewById(R.id.buttonstop);

        stopbutton.setEnabled(false);

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
        recordButton.setEnabled(false);
        stopbutton.setEnabled(true);
    }

    public void onStopRecording(View view) {
        recordButton.setEnabled(true);
        stopbutton.setEnabled(false);
    }
}