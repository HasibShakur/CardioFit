/**
 * 
 */
package com.example.DBConnection;

import com.example.DBConnection.DBTableContract.Profile;
import com.example.DBConnection.DBTableContract.Workout;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Hasib Shakur
 * This class creates the database tables and drops them
 */
public class DBHelper extends SQLiteOpenHelper{
	
	private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "cardiofit.db";
    private static final String INTEGER_TYPE = " INTEGER";
	private static final String TEXT_TYPE = " TEXT";
	private static final String REAL_TYPE = " REAL";
	private static final String COMMA_SEP = " ,";
	private static final String NOT_NULL_CONSTRAINT = " NOT NULL";
	/**
	 * Sql query for creating profile table
	 */
	private static final String SQL_CREATE_PROFILE_TABLE = 
			"CREATE TABLE "+ Profile.TABLE_NAME + " (" + 
	Profile.COLUMN_NAME_PERSON_ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT ," +
	Profile.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
	Profile.COLUMN_NAME_AGE + INTEGER_TYPE + COMMA_SEP +
	Profile.COLUMN_NAME_HEIGHT + REAL_TYPE + COMMA_SEP +
	Profile.COLUMN_NAME_WEIGHT + REAL_TYPE + COMMA_SEP +
	Profile.COLUMN_NAME_WEIGHT_MANAGE_HEART_RATE_HIGH + INTEGER_TYPE + COMMA_SEP +
	Profile.COLUMN_NAME_WEIGHT_MANAGE_HEART_RATE_LOW + INTEGER_TYPE + COMMA_SEP +
	Profile.COLUMN_NAME_AEROBIC_HEART_RATE_HIGH + INTEGER_TYPE + COMMA_SEP +
	Profile.COLUMN_NAME_AEROBIC_HEART_RATE_LOW + INTEGER_TYPE + " )";
	/**
	 * Sql query for creating workout table
	 */
	private static final String SQL_CREATE_WORKOUT_TABLE =
			"CREATE TABLE " + Workout.TABLE_NAME + " (" +
	Workout.COLUMN_NAME_ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT ," +
	Workout.COLUMN_NAME_PROFILE_ID +  INTEGER_TYPE + " REFERENCES " + Profile.TABLE_NAME + "("+ Profile.COLUMN_NAME_PERSON_ID +")" + COMMA_SEP +
	Workout.COLUMN_NAME_DATE + TEXT_TYPE + NOT_NULL_CONSTRAINT + COMMA_SEP +
	Workout.COLUMN_NAME_START_TIME + TEXT_TYPE + NOT_NULL_CONSTRAINT + COMMA_SEP +
	Workout.COLUMN_NAME_END_TIME + TEXT_TYPE + NOT_NULL_CONSTRAINT + COMMA_SEP + 
	Workout.COLUMN_NAME_HEART_RATE_HIGH + INTEGER_TYPE + NOT_NULL_CONSTRAINT + COMMA_SEP +
	Workout.COLUMN_NAME_HEART_RATE_LOW + INTEGER_TYPE + NOT_NULL_CONSTRAINT + COMMA_SEP +
	Workout.COLUMN_NAME_AVERAGE_HEART_RATE + INTEGER_TYPE + NOT_NULL_CONSTRAINT + COMMA_SEP +
	Workout.COLUMN_NAME_BURNED_CALORIES + REAL_TYPE + COMMA_SEP +
	Workout.COLUMN_NAME_DISTANCE + REAL_TYPE + COMMA_SEP +
	Workout.COLUMN_NAME_WORKOUT_TYPE + TEXT_TYPE + NOT_NULL_CONSTRAINT + " )";
	/**
	 * Sql query for dropping profile table
	 */
	private static final String SQL_DROP_PROFILE_TABLE =
			"DROP TABLE IF EXISTS " + Profile.TABLE_NAME;
	/**
	 * Sql query for dropping workout table
	 */
	private static final String SQL_DROP_WORKOUT_TABLE = 
			"DROP TABLE IF EXISTS " + Workout.TABLE_NAME;
    
    public DBHelper(Context context)
    {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
	@Override
	/**
	 * @param db
	 * When OnCreate function is called then profile and workout table's are created in db
	 */
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_PROFILE_TABLE);
		db.execSQL(SQL_CREATE_WORKOUT_TABLE);		
	}

	@Override
	/**
	 * @param db
	 * @param oldVersion
	 * @param newVersion
	 * When OnUpgrade function is called then profile and workout table's are dropped from db and new ones are created
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(SQL_DROP_WORKOUT_TABLE);
		db.execSQL(SQL_DROP_PROFILE_TABLE);
		onCreate(db);
	}
	/**
	 * @param db
	 * @param oldVersion
	 * @param newVersion
	 * This function just works the opposite of OnUpgrade
	 */
	public void onDownGrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
	
}
