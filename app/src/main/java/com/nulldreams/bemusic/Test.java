package com.nulldreams.bemusic;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class Test extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {

       // super.onCreate(savedInstanceState);
        //setContentView(R.layout.sleep_dialog);
        super.onCreate(savedInstanceState, persistentState);
      //  setContentView(R.layout.fragment_settings);
        setContentView(R.layout.sleep_dialog);
    }
}
