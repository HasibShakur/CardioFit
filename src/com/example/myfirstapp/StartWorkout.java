package com.example.myfirstapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;

public class StartWorkout extends Activity {
    public final static String WORKOUT_TYPE = "com.example.myfirstapp.MESSAGE";
	private static final String TAG = "StartWorkout";
	private static int REQUEST_ENABLE_BT = 1;


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "[ACTIVITY] onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_workout);

		//Set use Heart Monitor to true by default
		final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox1);
		checkBox.setChecked(true);
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

	
	public void beginWorkout(View view) {
		final Spinner workout_type_spinner = (Spinner) findViewById(R.id.spinner1);
		final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox1);
        if (checkBox.isChecked()) {
        	
        	Log.i(TAG, "user requested heart monitor");
        	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        	if (mBluetoothAdapter == null) {
        		Log.i(TAG, "Device does not support Bluetooth");
        	}
        	if (!mBluetoothAdapter.isEnabled()) {
        		Log.i(TAG, "Bluetooth not enabled");
        	    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        	} else {
       			startWorkout();
        	}
        } else {
        	Log.i(TAG, "user requested no heart monitor");
			startWorkout();
        }
	}
	
	//This function is called upon user's answer to turning on BlueTooth
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			startWorkout();
		}
	}
	
	protected void startWorkout() {
		final Spinner workout_type_spinner = (Spinner) findViewById(R.id.spinner1);
		Intent intent = new Intent(this, Workout.class);
		String workout_type = workout_type_spinner.getSelectedItem().toString();
		intent.putExtra(WORKOUT_TYPE, workout_type);
		startActivity(intent);
	}
}
