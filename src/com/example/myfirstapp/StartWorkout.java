package com.example.myfirstapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class StartWorkout extends Activity {
    public final static String WORKOUT_TYPE = "com.example.myfirstapp.MESSAGE";

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_workout);

		final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox1);
		checkBox.setChecked(true);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void playMusic(View view) {
		@SuppressWarnings("deprecation")
		Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
		startActivity(intent);
	}
	
	public void beginWorkout(View view) {
		final Spinner workout_type_spinner = (Spinner) findViewById(R.id.spinner1);
		final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox1);
        if (checkBox.isChecked()) {
        	//Need to set up the bluetooth cardio tracker
        }
		if (workout_type_spinner.getSelectedItem().toString().equals("running")) {
			//do the running workout
			//this may include gps if we want
		}

		
		
		Intent intent = new Intent(this, Workout.class);
		String workout_type = workout_type_spinner.getSelectedItem().toString();
		intent.putExtra(WORKOUT_TYPE, workout_type);
		startActivity(intent);
		
	}
}
