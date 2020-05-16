package com.esp1920.lookandpick;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/**
 * This is the main activity of LookAndPick: the user is in a room and different objects will
 * appear randomly. When in Cardboard mode, the user has to pick them up by looking them and
 * triggering the Cardboard button.
 */
public class MainActivity extends AppCompatActivity {

    //private static final String OBJECT_SOUND_FILE = "audio/HelloVR_Loop.ogg";
    //private static final String SUCCESS_SOUND_FILE = "audio/HelloVR_Activation.ogg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
