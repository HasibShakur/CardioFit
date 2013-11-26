package com.example.myfirstapp;



import java.sql.Time;
import java.util.ArrayList;

import com.example.DBConnection.DBOperateDAO;
import com.example.DBConnection.ProfileDTO;
import com.example.DBConnection.WorkoutDTO;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.widget.ExpandableListView;


public class WorkoutHistory extends Activity {
	public String TAG = "WorkoutHistory";
	
	private DBOperateDAO operatorDao;

	
	// more efficient than HashMap for mapping integers to objects
	  SparseArray<Group> groups = new SparseArray<Group>();

	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.workout_history);
	    createData();
	    Log.i(TAG, "" + groups);
	    Log.i(TAG, "groups.size() = " + groups.size());
	    ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
	    Log.i(TAG, "listView = " + listView);
	    Log.i(TAG, groups.get(0).string);
	    MyExpandableListAdapter adapter = new MyExpandableListAdapter(this, groups);
	    Log.i(TAG, "adapter = " + adapter);
	    listView.setAdapter(adapter);
	  }

	  public void createData() {
		  //create the DAO class object here
		  operatorDao = new DBOperateDAO(this);
		  //open Database connection
		  operatorDao.openDatabase();
	
		  //Get the items from view & set their initial values (if they exist)
		  ArrayList<WorkoutDTO> workouts = new ArrayList<WorkoutDTO>();

		  workouts = operatorDao.getAllWorkouts();
		  Log.i(TAG, "workouts.size() = " + workouts.size());
		  
		  int j = 0;
		  for (WorkoutDTO workout : workouts) {
			  Group group = new Group(workout.getWorkoutType() + ", " + workout.getWorkoutDate().toString().substring(0,10));
			  long ms = workout.getWorkoutEnd().getTime() - workout.getWorkoutStart().getTime();
			  long durationInMinutes = ms / (60 * 1000);

			  group.children.add("Duration: " + durationInMinutes + " mins");
			  group.children.add("Max Heart Rate: " + workout.getHighHeartRate());
			  group.children.add("Min Heart Rate: " + workout.getLowHeartRate());
			  //group.children.add("Avg Heart Rate: " + workout.getAverageHeartRate());
			  //gropu.children.add("Time within desired range: " + workout.getTimeWithinRange());
			  group.children.add("Burned Calores: " + workout.getBurnedCalories());
		      groups.append(j, group);
			  j++;

	    }
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
}