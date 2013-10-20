/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myfirstapp;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */
public class Workout extends Activity {
    // Debugging
    private static final String TAG = "Workout";
    private static final boolean D = true;

    
    //Timer
    private Chronometer mChronometer;
    long timeWhenClicked = 0;
    boolean isChronometerRunning = false;

    
    private TextView mWorkoutType;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        // Set up the window layout
        setContentView(R.layout.workout);
        
        Intent intent = getIntent();
        String workout_type = intent.getStringExtra(StartWorkout.WORKOUT_TYPE);
        
        mWorkoutType = (TextView) findViewById(R.id.workout_type);
        mWorkoutType.setText(workout_type);
        
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mChronometer.start();
        isChronometerRunning = true;

    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }
    
	public void playMusic(View view) {
		@SuppressWarnings("deprecation")
		Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
		startActivity(intent);
	}
	
	public void startTimer(View view) {
		if (!isChronometerRunning) {
			mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenClicked);
			timeWhenClicked = mChronometer.getBase() - SystemClock.elapsedRealtime();
			mChronometer.start();
			isChronometerRunning = true;
		}
	}
	
	public void stopTimer (View view) {
		if (isChronometerRunning) {
			timeWhenClicked = mChronometer.getBase() - SystemClock.elapsedRealtime();
			mChronometer.stop();
			isChronometerRunning = false;
		}
	}
	
	public void resetTimer (View view) {
		mChronometer.setBase(SystemClock.elapsedRealtime());
		if (isChronometerRunning) {
			mChronometer.stop();
			isChronometerRunning = false;
		}
		timeWhenClicked = 0;
	}

}
