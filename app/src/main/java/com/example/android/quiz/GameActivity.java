package com.example.android.quiz;

import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private static ArrayList<String[]> data = new ArrayList<>();
    private static ArrayList<String[]> questions = new ArrayList<>();

    boolean[] isCorrect = new boolean[5];
    long start;
    long end;

    boolean clicked;

    long[] time = new long[5];
    char[] your_ans = new char[5];
    double[] score = new double[5];
    String[] correct_ans = new String[5];

    private TextView optA;
    private TextView optB;
    private TextView optC;
    private TextView optD;
    private TextView countDown;

    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Bundle extras = getIntent().getExtras();
        int[] arr = new int[5];
        if (extras != null) {
            arr = extras.getIntArray("questions");
        }

        optA = findViewById(R.id.optA);
        optB = findViewById(R.id.optB);
        optC = findViewById(R.id.optC);
        optD = findViewById(R.id.optD);
        countDown = findViewById(R.id.count_down);

        startGame(arr);
        for (int i = 0; i < 5; i++) {
            your_ans[i] = '-';
            correct_ans[i] = questions.get(i)[5];
        }
        printQuestion();
    }

    public void startGame(int[] arr) {
        InputStream inputStream = getResources().openRawResource(R.raw.questions);

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split(",");
                data.add(row);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }

        for (int i: arr) {
            questions.add(data.get(i - 1));
        }
    }

    public void printQuestion() {
        TextView textView = findViewById(R.id.question);
        if (count < 5) {
            startCountDown(countDown, 3);
            textView.setText(questions.get(count)[0]);
            optA.setText(questions.get(count)[1]);
            optB.setText(questions.get(count)[2]);
            optC.setText(questions.get(count)[3]);
            optD.setText(questions.get(count)[4]);

            count++;
            start = System.currentTimeMillis();
            Log.i("Start: ", String.format("%d", start));
            clicked = false;

            Utils.delay(3, new Utils.DelayCallback() {
                @Override
                public void afterDelay() {
                    enableListners();
                    optA.setBackgroundColor(Color.parseColor("#ffffff"));
                    optB.setBackgroundColor(Color.parseColor("#ffffff"));
                    optC.setBackgroundColor(Color.parseColor("#ffffff"));
                    optD.setBackgroundColor(Color.parseColor("#ffffff"));
                    printQuestion();
                }
            });
        }
        else {
            for (int i = 0; i < 5; i++) {
                if (your_ans[i] == '-') {
                    time[i] = 3000;
                }

                if (isCorrect[i]) {
                    score[i] = 100.0 + (1.0 * (10000 - time[i]) / 1000);
                } else {
                    score[i] = 0.0;
                }
            }

            Intent intent = new Intent(this, ScoreActivity.class);
            intent.putExtra("your_ans", your_ans);
            intent.putExtra("correct_ans", correct_ans);
            intent.putExtra("time", time);
            intent.putExtra("score", score);
            startActivity(intent);
        }
    }

    public void optAClicked(View view) {
        if (count <= 5 && !clicked) {
            clicked = true;
            end = System.currentTimeMillis();
            time[count - 1] = end - start;

            your_ans[count - 1] = 'A';
            if (questions.get(count - 1)[5].equals(String.format("%c", 'A'))) {
                optA.setBackgroundColor(Color.parseColor("#00ff00"));
                isCorrect[count - 1] = true;
            }
            else {
                if (correct_ans[count - 1].equals(String.format("%c", 'B'))) {
                    optB.setBackgroundColor(Color.parseColor("#00ff00"));
                }
                else if (correct_ans[count - 1].equals(String.format("%c", 'C'))) {
                    optC.setBackgroundColor(Color.parseColor("#00ff00"));
                }
                else {
                    optD.setBackgroundColor(Color.parseColor("#00ff00"));
                }

                optA.setBackgroundColor(Color.parseColor("#ff0000"));
                isCorrect[count - 1] = false;
            }
            disableListners();
        }
    }

    public void optBClicked(View view) {
        if (count <= 5 && !clicked) {
            clicked = true;
            end = System.currentTimeMillis();
            time[count - 1] = end - start;

            your_ans[count - 1] = 'B';
            if (questions.get(count - 1)[5].equals(String.format("%c", 'B'))) {
                optB.setBackgroundColor(Color.parseColor("#00ff00"));
                isCorrect[count - 1] = true;
            }
            else {
                if (correct_ans[count - 1].equals(String.format("%c", 'A'))) {
                    optA.setBackgroundColor(Color.parseColor("#00ff00"));
                }
                else if (correct_ans[count - 1].equals(String.format("%c", 'C'))) {
                    optC.setBackgroundColor(Color.parseColor("#00ff00"));
                }
                else {
                    optD.setBackgroundColor(Color.parseColor("#00ff00"));
                }
                optB.setBackgroundColor(Color.parseColor("#ff0000"));
                isCorrect[count - 1] = false;
            }

            disableListners();
        }
    }

    public void optCClicked(View view) {
        if (count <= 5 && !clicked) {
            clicked = true;
            end = System.currentTimeMillis();
            time[count - 1] = end - start;

            your_ans[count - 1] = 'C';
            if (questions.get(count - 1)[5].equals(String.format("%c", 'C'))) {
                optC.setBackgroundColor(Color.parseColor("#00ff00"));
                isCorrect[count - 1] = true;
            }
            else {
                if (correct_ans[count - 1].equals(String.format("%c", 'A'))) {
                    optA.setBackgroundColor(Color.parseColor("#00ff00"));
                }
                else if (correct_ans[count - 1].equals(String.format("%c", 'B'))) {
                    optB.setBackgroundColor(Color.parseColor("#00ff00"));
                }
                else {
                    optD.setBackgroundColor(Color.parseColor("#00ff00"));
                }
                optC.setBackgroundColor(Color.parseColor("#ff0000"));
                isCorrect[count - 1] = false;
            }

            disableListners();
        }
    }

    public void optDClicked(View view) {
        if (count <= 5 && !clicked) {
            clicked = true;
            end = System.currentTimeMillis();
            time[count - 1] = end - start;

            your_ans[count - 1] = 'D';
            if (questions.get(count - 1)[5].equals(String.format("%c", 'D'))) {
                optD.setBackgroundColor(Color.parseColor("#00ff00"));
                isCorrect[count - 1] = true;
            }
            else {
                if (correct_ans[count - 1].equals(String.format("%c", 'A'))) {
                    optA.setBackgroundColor(Color.parseColor("#00ff00"));
                }
                else if (correct_ans[count - 1].equals(String.format("%c", 'B'))) {
                    optB.setBackgroundColor(Color.parseColor("#00ff00"));
                }
                else {
                    optC.setBackgroundColor(Color.parseColor("#00ff00"));
                }
                optD.setBackgroundColor(Color.parseColor("#ff0000"));
                isCorrect[count - 1] = false;
            }

            disableListners();
        }
    }

    public void disableListners() {
        optA.setClickable(false);
        optB.setClickable(false);
        optC.setClickable(false);
        optD.setClickable(false);
    }

    public void enableListners() {
        optA.setClickable(true);
        optB.setClickable(true);
        optC.setClickable(true);
        optD.setClickable(true);
    }

    public void startCountDown(TextView textView, int secs) {
        final TextView countDownTextView = textView;
        final int secsToWait = secs;
        new CountDownTimer(secsToWait * 1000 + 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                countDownTextView.setText("" + millisUntilFinished / 1000);
            }
            public void onFinish() {}
        }.start();
    }
}
