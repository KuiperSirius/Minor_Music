package com.nulldreams.bemusic;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;


import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lantouzi.wheelview.WheelView;

import java.util.ArrayList;
import java.util.List;

public class SleepActivity extends AppCompatActivity implements View.OnClickListener {

    Button exit;
    Button apply;
    Intent intent;

    public static int themeColor = Color.parseColor("#B24242");
    boolean isSleepTimerEnabled = false;
    public Context ctx;
    public static NotificationManager notificationManager;
    public Activity main;
    private boolean isResumed;
    List<String> minuteList; //分钟
    boolean isSleepTimerTimeout = false;
    long timerSetTime = 0;
    int timerTimeOutDuration = 0;
    Handler sleepHandler;


    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
       setContentView(R.layout.sleep_dialog);

       // setContentView(R.layout.fragment_settings);

        minuteList = new ArrayList<>();
        for (int i = 1; i < 25; i++) {
            minuteList.add(String.valueOf(i * 5));
        }

        sleepHandler = new Handler();

        exit=(Button)findViewById(R.id.cancel_button);
        apply=(Button)findViewById(R.id.set_button);

exit.setOnClickListener(this);
apply.setOnClickListener(this);
        showSleepDialog();
    }
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.cancel_button:
                intent=getIntent();
                setResult(0x11,intent);
                finish();
                break;
            case R.id.set_button:
                intent=getIntent();
                setResult(0x11,intent);
                finish();
                break;
        }
    }
    public void showSleepDialog() {

        final WheelView wheelPicker = (WheelView)findViewById(R.id.wheelPicker);
        wheelPicker.setItems(minuteList);

        TextView title = (TextView)findViewById(R.id.sleep_dialog_title_text);


        Button setBtn = (Button)findViewById(R.id.set_button);
        Button cancelBtn = (Button)findViewById(R.id.cancel_button);
        final Button removerBtn = (Button)findViewById(R.id.remove_timer_button);

        final LinearLayout buttonWrapper = (LinearLayout)findViewById(R.id.button_wrapper);

        final TextView timerSetText = (TextView)findViewById(R.id.timer_set_text);

        setBtn.setBackgroundColor(themeColor);
        removerBtn.setBackgroundColor(themeColor);
        cancelBtn.setBackgroundColor(Color.WHITE);

        if (isSleepTimerEnabled) {
            wheelPicker.setVisibility(View.GONE);
            buttonWrapper.setVisibility(View.GONE);
            removerBtn.setVisibility(View.VISIBLE);
            timerSetText.setVisibility(View.VISIBLE);

            long currentTime = System.currentTimeMillis();
            long difference = currentTime - timerSetTime;

            int minutesLeft = (int) (timerTimeOutDuration - ((difference / 1000) / 60));
            if (minutesLeft > 1) {
                timerSetText.setText("Timer set for " + minutesLeft + " minutes from now.");
            } else if (minutesLeft == 1) {
                timerSetText.setText("Timer set for " + 1 + " minute from now.");
            } else {
                timerSetText.setText("Music will stop after completion of current song");
            }

        } else {
            wheelPicker.setVisibility(View.VISIBLE);
            buttonWrapper.setVisibility(View.VISIBLE);
            removerBtn.setVisibility(View.GONE);
            timerSetText.setVisibility(View.GONE);
        }

        removerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSleepTimerEnabled = false;
                isSleepTimerTimeout = false;
                timerTimeOutDuration = 0;
                timerSetTime = 0;
                sleepHandler.removeCallbacksAndMessages(null);
                Toast.makeText(ctx, "Timer removed", Toast.LENGTH_SHORT).show();

            }
        });

        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSleepTimerEnabled = true;
                int minutes = Integer.parseInt(wheelPicker.getItems().get(wheelPicker.getSelectedPosition()));
                timerTimeOutDuration = minutes;
                timerSetTime = System.currentTimeMillis();
                sleepHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isSleepTimerTimeout = true;

                            main.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ctx, "Sleep timer timed out, closing app", Toast.LENGTH_SHORT).show();

                                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    try {
                                        notificationManager.cancel(1);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        finish();
                                    }
                                }
                            });

                    }
                }, minutes * 60 * 1000);
                Toast.makeText(ctx, "Timer set for " + minutes + " minutes", Toast.LENGTH_SHORT).show();

                intent=getIntent();
                setResult(0x11,intent);
                finish();


            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSleepTimerEnabled = false;
                isSleepTimerTimeout = false;
                intent=getIntent();
                setResult(0x11,intent);
                finish();
            }
        });



    }


}
