package com.nulldreams.bemusic;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class Configuration extends AppCompatActivity  {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
       setContentView(R.layout.fragment_settings);
       // setContentView(R.layout.sleep_dialog);
    }
}
