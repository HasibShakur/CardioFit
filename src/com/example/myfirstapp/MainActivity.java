package com.example.myfirstapp;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public final static String TAG = "MainActivity";
	//public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);

	    getActionBar().setDisplayHomeAsUpEnabled(true);

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
    
    /**
     * Called When user clicks 'Start Workout'
     * Opens the start_workout activity
     * @param view
     * @author: Wyatt Kaiser
     */
	public void startWorkout(View view) {
		Intent intent = new Intent(this, StartWorkout.class);
		startActivity(intent);
	}
	
	/**
	 * Called when a user clicks 'Edit Profile'
	 * Opens the edit profile activity
	 * @param view
	 * @author: Wyatt Kaiser
	 */
	public void editProfile(View view) {
		Intent intent = new Intent(this, EditProfile.class);
		startActivity(intent);
	}
	
	/**
	 * Called when a user clicks 'View History'
	 * Opens the workout history activity
	 * @param view
	 * @author: Wyatt Kaiser
	 */
	public void viewHistory(View view) {
		Intent intent = new Intent(this, WorkoutHistory.class);
		startActivity(intent);
	}
}


