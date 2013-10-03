package com.example.myfirstapp;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_main);

	    getActionBar().setDisplayHomeAsUpEnabled(true);

	}
	
	public void startWorkout(View view) {
		//do something in response to button
		Intent intent = new Intent(this, StartWorkout.class);
		intent.putExtra(EXTRA_MESSAGE, "START WORKOUT");
		startActivity(intent);
	}
	
	public void editProfile(View view) {
		//do something in response to button
		Intent intent = new Intent(this, EditProfile.class);
		//EditText editText = (EditText) findViewById(R.id.edit_message);
		//String message = editText.getText().toString();
		intent.putExtra(EXTRA_MESSAGE, "EDIT PROFILE");
		startActivity(intent);
	}
	
	public void fitnessTest(View view) {
		//do something in response to button
		Intent intent = new Intent(this, FitnessTest.class);
		//EditText editText = (EditText) findViewById(R.id.edit_message);
		//String message = editText.getText().toString();
		intent.putExtra(EXTRA_MESSAGE, "FITNESS TEST");
		startActivity(intent);
	}
}


