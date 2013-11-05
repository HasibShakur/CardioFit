/**
 * 
 */
package com.example.DBConnection;

import android.provider.BaseColumns;

/**
 * @author Hasib Shakur
 * This class contains the schema of the database
 * Each inner class represents a separate table
 */
public final class DBTableContract {
	
	/**
	 * In order to preventing accidental instantiation the construction of this cluss is empty
	 */
	public DBTableContract() {}
	
	public static abstract class Profile implements BaseColumns
	{
		public static final String TABLE_NAME = "profile";
		public static final String COLUMN_NAME_PERSON_ID = "personId";
		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_AGE = "age";
		public static final String COLUMN_NAME_WEIGHT = "weight";
		public static final String COLUMN_NAME_HEIGHT = "height";
		public static final String COLUMN_NAME_WEIGHT_MANAGE_HEART_RATE_HIGH ="weightManagementHeartRateHigh";
		public static final String COLUMN_NAME_WEIGHT_MANAGE_HEART_RATE_LOW ="weightManagementHeartRateLow";
		public static final String COLUMN_NAME_AEROBIC_HEART_RATE_HIGH ="aerobicHeartRateHigh";
		public static final String COLUMN_NAME_AEROBIC_HEART_RATE_LOW ="aerobicHeartRateLow";
	}
	public static abstract class Workout implements BaseColumns
	{
		public static final String TABLE_NAME = "workout";
		public static final String COLUMN_NAME_ID = "id";
		public static final String COLUMN_NAME_PROFILE_ID = "profileId";
		public static final String COLUMN_NAME_DATE = "date";
		public static final String COLUMN_NAME_START_TIME = "startTime";
		public static final String COLUMN_NAME_END_TIME = "endTime";
		public static final String COLUMN_NAME_HEART_RATE_HIGH = "heartRateHigh";
		public static final String COLUMN_NAME_HEART_RATE_LOW = "heartRateLow";
		public static final String COLUMN_NAME_DISTANCE = "distance";
		public static final String COLUMN_NAME_BURNED_CALORIES = "calories";
		public static final String COLUMN_NAME_AVERAGE_HEART_RATE = "averageHeartRateHigh";
		public static final String COLUMN_NAME_WORKOUT_TYPE = "workoutType";
	}
}
