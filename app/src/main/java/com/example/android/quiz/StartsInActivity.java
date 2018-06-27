package com.example.android.quiz;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class StartsInActivity extends AppCompatActivity {

    TextView startsIn;
    int[] arr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starts_in);

        Bundle extras = getIntent().getExtras();
        arr = new int[5];
        if (extras != null) {
            arr = extras.getIntArray("questions");
        }

        startsIn = findViewById(R.id.starts_in);
        startCountDown(startsIn, 5);
    }

    public void startCountDown(TextView textView, int secs) {
        final TextView countDownTextView = textView;
        final int secsToWait = secs;
        new CountDownTimer(secsToWait * 1000 + 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                countDownTextView.setText("" + millisUntilFinished / 1000);
            }
            public void onFinish() {
                Intent intent = new Intent(StartsInActivity.this, GameActivity.class);
                intent.putExtra("questions", arr);
                startActivity(intent);
            }
        }.start();
    }
}
