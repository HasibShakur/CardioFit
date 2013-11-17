

package com.example.myfirstapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;

public class StartWorkout extends Activity {
    public final static String WORKOUT_TYPE = "com.example.myfirstapp.MESSAGE";
    public final static String HEART_RANGE_TYPE = "com.example.myirstapp.MESSAGE";
    
	private static final String TAG = "StartWorkout2";
	
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    @SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "[ACTIVITY] onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_workout);
	}

	@Override
    protected void onStart() {
        Log.i(TAG, "[ACTIVITY] onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "[ACTIVITY] onResume");
        super.onResume();        
    }
    
    
    @Override
    protected void onPause() {
        Log.i(TAG, "[ACTIVITY] onPause");
        super.onPause();
    }
    
    @Override
    protected void onStop() {
        Log.i(TAG, "[ACTIVITY] onStop");
        super.onStop();
    }

    protected void onDestroy() {
        Log.i(TAG, "[ACTIVITY] onDestroy");
        super.onDestroy();
    }
    
    protected void onRestart() {
        Log.i(TAG, "[ACTIVITY] onRestart");
        super.onDestroy();
    }


    
	
	public void startWorkout(View view) {
		final Spinner workout_type_spinner = (Spinner) findViewById(R.id.workout_spinner);
		final Spinner heart_range_type_spinner = (Spinner) findViewById(R.id.heartRange_spinner);
		
		Intent intent = new Intent(this, Workout.class);
		
		String workout_type = workout_type_spinner.getSelectedItem().toString();
		intent.putExtra(WORKOUT_TYPE, workout_type);
		
		String heartRange_type = heart_range_type_spinner.getSelectedItem().toString();
		intent.putExtra(HEART_RANGE_TYPE, heartRange_type);
		startActivity(intent);
	}
}
