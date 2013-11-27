/**
 * 
 */
package com.example.DBConnection;

import java.sql.Time;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.example.myfirstapp.Util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author Hasib Shakur
 * This is the main class for database operation.
 * It inserts value to the profile and workout class, deletes values from them and performs update
 * operation on them
 */
public class DBOperateDAO {
	private SQLiteDatabase database;
	private DBHelper helper;
	private String[] profileColumns = { DBTableContract.Profile.COLUMN_NAME_PERSON_ID, DBTableContract.Profile.COLUMN_NAME_NAME,
			DBTableContract.Profile.COLUMN_NAME_AGE, DBTableContract.Profile.COLUMN_NAME_HEIGHT,
			DBTableContract.Profile.COLUMN_NAME_WEIGHT, DBTableContract.Profile.COLUMN_NAME_GENDER	
	};
	private String[] workoutColumns = { DBTableContract.Workout.COLUMN_NAME_ID , DBTableContract.Workout.COLUMN_NAME_PROFILE_ID, DBTableContract.Workout.COLUMN_NAME_DATE,
			DBTableContract.Workout.COLUMN_NAME_START_TIME, DBTableContract.Workout.COLUMN_NAME_END_TIME,
			DBTableContract.Workout.COLUMN_NAME_HEART_RATE_HIGH, DBTableContract.Workout.COLUMN_NAME_HEART_RATE_LOW,
			DBTableContract.Workout.COLUMN_NAME_BURNED_CALORIES,
			DBTableContract.Workout.COLUMN_NAME_TIME_WITHIN_RANGE, DBTableContract.Workout.COLUMN_NAME_WORKOUT_TYPE, 
			DBTableContract.Workout.COLUMN_NAME_AVG_HEART_RATE
	};
	
	public DBOperateDAO(Context context)
	{
		helper = new DBHelper(context);
	}
	public void openDatabase() throws SQLException
	{
		database = helper.getWritableDatabase();
	}
	public void closeDatabase()
	{
		helper.close();
	}
	public void createProfile(ProfileDTO profile)
	{
		System.out.println("Operation performed");
		ContentValues values = new ContentValues();
		values.put(DBTableContract.Profile.COLUMN_NAME_NAME, profile.getPersonName());
		values.put(DBTableContract.Profile.COLUMN_NAME_AGE, profile.getPersonAge());
		values.put(DBTableContract.Profile.COLUMN_NAME_HEIGHT, profile.getHeight());
		values.put(DBTableContract.Profile.COLUMN_NAME_WEIGHT, profile.getWeight());
		values.put(DBTableContract.Profile.COLUMN_NAME_GENDER, profile.getGender());
		database.insert(DBTableContract.Profile.TABLE_NAME, null, values);
	}	
	public void updateProfile(ProfileDTO profile)
	{
		System.out.println("Operation performed");
		ContentValues values = new ContentValues();
		values.put(DBTableContract.Profile.COLUMN_NAME_NAME, profile.getPersonName());
		values.put(DBTableContract.Profile.COLUMN_NAME_AGE, profile.getPersonAge());
		values.put(DBTableContract.Profile.COLUMN_NAME_HEIGHT, profile.getHeight());
		values.put(DBTableContract.Profile.COLUMN_NAME_WEIGHT, profile.getWeight());
		values.put(DBTableContract.Profile.COLUMN_NAME_GENDER, profile.getGender());
		database.update(DBTableContract.Profile.TABLE_NAME, values, null, null);
	}
	
	public void deleteProfile(ProfileDTO profile)
	{
		long id = profile.getPersonId();
		System.out.println("Profile deleted with id: "+id);
		database.delete(DBTableContract.Profile.TABLE_NAME, DBTableContract.Profile.COLUMN_NAME_PERSON_ID + " = " +  id, null);
	}
	public ArrayList<ProfileDTO> getAllProfiles()
	{
		ArrayList<ProfileDTO> profiles = new ArrayList<ProfileDTO>();
		Cursor cursor = database.query(DBTableContract.Profile.TABLE_NAME,
		        profileColumns, null, null, null, null, null);

		 cursor.moveToFirst();
		 while (!cursor.isAfterLast()) {
			ProfileDTO profile = cursorToProfile(cursor);
			profiles.add(profile);
		    cursor.moveToNext();
		 }
		 cursor.close();
		 return profiles;
	}
	public void CreateWorkout(WorkoutDTO workout)
	{
		ContentValues values = new ContentValues();
		values.put(DBTableContract.Workout.COLUMN_NAME_PROFILE_ID, workout.getProfileId());
		values.put(DBTableContract.Workout.COLUMN_NAME_DATE, workout.getWorkoutDate().toString());
		values.put(DBTableContract.Workout.COLUMN_NAME_START_TIME, workout.getWorkoutStart().toString());
		values.put(DBTableContract.Workout.COLUMN_NAME_END_TIME, workout.getWorkoutEnd().toString());
		values.put(DBTableContract.Workout.COLUMN_NAME_HEART_RATE_HIGH, workout.getHighHeartRate());
		values.put(DBTableContract.Workout.COLUMN_NAME_HEART_RATE_LOW, workout.getLowHeartRate());
		values.put(DBTableContract.Workout.COLUMN_NAME_BURNED_CALORIES, workout.getBurnedCalories());
		values.put(DBTableContract.Workout.COLUMN_NAME_TIME_WITHIN_RANGE, workout.getTimeWithinRange());
		values.put(DBTableContract.Workout.COLUMN_NAME_WORKOUT_TYPE, workout.getWorkoutType());
		values.put(DBTableContract.Workout.COLUMN_NAME_AVG_HEART_RATE, workout.getAverageHeartRate());
		database.insert(DBTableContract.Workout.TABLE_NAME, null, values);
	}
	public void deleteWorkout(WorkoutDTO workout)
	{
		long id = workout.getId();
		System.out.println("Workout deleted with id: "+id);
		database.delete(DBTableContract.Profile.TABLE_NAME, DBTableContract.Workout.COLUMN_NAME_ID + " = " +  id, null);
	}
	public ArrayList<WorkoutDTO> getAllWorkouts()
	{
		ArrayList<WorkoutDTO> workouts = new ArrayList<WorkoutDTO>();
		Cursor cursor = database.query(DBTableContract.Workout.TABLE_NAME,
		        workoutColumns, null, null, null, null, null);

		 cursor.moveToFirst();
		 while (!cursor.isAfterLast()) {
			WorkoutDTO workout = cursorToWorkout(cursor);
			workouts.add(workout);
		    cursor.moveToNext();
		 }
		 cursor.close();
		 return workouts;
	}
	private ProfileDTO cursorToProfile(Cursor c)
	{
		ProfileDTO p = new ProfileDTO();
		p.setPersonId(c.getLong(0));
		p.setPersonName(c.getString(1));
		p.setPersonAge(c.getInt(2));
		p.setHeight(c.getDouble(3));
		p.setWeight(c.getDouble(4));
		p.setGender(c.getString(5));
		return p;
	}
	@SuppressWarnings("deprecation")
	private WorkoutDTO cursorToWorkout(Cursor c)
	{
		WorkoutDTO w = new WorkoutDTO();
		w.setId(c.getLong(0));
		w.setProfileId(c.getLong(1));
		String date = c.getString(2);
		String formatted_date = "00/00/0000";
		try {
			formatted_date = Util.dateConversion(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String[] splitter;
		splitter = formatted_date.split("/");
		
		Date d = new Date();
		d.setDate(Integer.parseInt(splitter[0]));
		d.setMonth(Integer.parseInt(splitter[1]));
		d.setYear(Integer.parseInt(splitter[2]));
		w.setWorkoutDate(d);
		
		w.setWorkoutStart(Time.valueOf(c.getString(3)));
		w.setWorkoutEnd(Time.valueOf(c.getString(4)));
		w.setHighHeartRate(c.getInt(5));
		w.setLowHeartRate(c.getInt(6));
		w.setBurnedCalories(c.getDouble(7));
		w.setTimeWithinRange(c.getDouble(8));
		w.setWorkoutType(c.getString(9));
		w.setAverageHeartRate(c.getDouble(10));
		return w;
		
	}
	

}
