package com.example.android.quiz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class ScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        Bundle extras = getIntent().getExtras();
        long[] time = new long[5];
        char[] your_ans = new char[5];
        double[] score = new double[5];
        String[] correct_ans = new String[5];

        if (extras != null) {
            time = extras.getLongArray("time");
            your_ans = extras.getCharArray("your_ans");
            score = extras.getDoubleArray("score");
            correct_ans = extras.getStringArray("correct_ans");
        }

        double finalScore = 0;
        for (double scr: score) {
            finalScore += scr;
        }

//        TextView your_ans_1 = findViewById(R.id.your_ans_1);
//        your_ans_1.setText(String.format("%c", your_ans[0]));
//        TextView your_ans_2 = findViewById(R.id.your_ans_2);
//        your_ans_2.setText(String.format("%c", your_ans[1]));
//        TextView your_ans_3 = findViewById(R.id.your_ans_3);
//        your_ans_3.setText(String.format("%c", your_ans[2]));
//        TextView your_ans_4 = findViewById(R.id.your_ans_4);
//        your_ans_4.setText(String.format("%c", your_ans[3]));
//        TextView your_ans_5 = findViewById(R.id.your_ans_5);
//        your_ans_5.setText(String.format("%c", your_ans[4]));
//
//        TextView correct_ans_1 = findViewById(R.id.correct_ans_1);
//        correct_ans_1.setText(correct_ans[0]);
//        TextView correct_ans_2 = findViewById(R.id.correct_ans_2);
//        correct_ans_2.setText(correct_ans[1]);
//        TextView correct_ans_3 = findViewById(R.id.correct_ans_3);
//        correct_ans_3.setText(correct_ans[2]);
//        TextView correct_ans_4 = findViewById(R.id.correct_ans_4);
//        correct_ans_4.setText(correct_ans[3]);
//        TextView correct_ans_5 = findViewById(R.id.correct_ans_5);
//        correct_ans_5.setText(correct_ans[4]);

        TextView time_1 = findViewById(R.id.time_1);
        time_1.setText(String.format("%.3f", (1.0 * time[0])/1000));
        TextView time_2 = findViewById(R.id.time_2);
        time_2.setText(String.format("%.3f", (1.0 * time[1])/1000));
        TextView time_3 = findViewById(R.id.time_3);
        time_3.setText(String.format("%.3f", (1.0 * time[2])/1000));
        TextView time_4 = findViewById(R.id.time_4);
        time_4.setText(String.format("%.3f", (1.0 * time[3])/1000));
        TextView time_5 = findViewById(R.id.time_5);
        time_5.setText(String.format("%.3f", (1.0 * time[4])/1000));

        TextView score_1 = findViewById(R.id.score_1);
        score_1.setText(String.format("%.3f", score[0]));
        TextView score_2 = findViewById(R.id.score_2);
        score_2.setText(String.format("%.3f", score[1]));
        TextView score_3 = findViewById(R.id.score_3);
        score_3.setText(String.format("%.3f", score[2]));
        TextView score_4 = findViewById(R.id.score_4);
        score_4.setText(String.format("%.3f", score[3]));
        TextView score_5 = findViewById(R.id.score_5);
        score_5.setText(String.format("%.3f", score[4]));

        TextView final_score = findViewById(R.id.final_score);
        final_score.setText(String.format("%.3f", finalScore));

        // deactivate leaderBoard

        // if group owner
        // receive scores and playerName from all the players and store in arrayList
        // if length of arrayList is equal to no. of clients then send the arrayTo all the players
        // turn leaderBoard on

        // if player
        // send score along with player name
        // once the arrayList is received turn on leaderBoard


    }

    public void play_again(View view) {
        Intent intent = new Intent(ScoreActivity.this, MainActivity.class);
        startActivity(intent);
    }
}