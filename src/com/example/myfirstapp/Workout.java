package com.example.myfirstapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

public class Workout extends Activity {
	private static final String TAG = "CardioFit";
	


	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.workout);

		// Get the message from the intent
	    Intent intent = getIntent();
	    String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		final TextView workout_type = (TextView) findViewById(R.id.workout_type);

		//Log.i("message" , message);
		//Log.i("TextView" , "workout_type = " + workout_type);
		workout_type.setText(message);


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


	public void playMusic(View view) {
		@SuppressWarnings("deprecation")
		Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
		startActivity(intent);
	}
}

