/**
 * 
 */
package com.example.DBConnection;

import java.sql.Time;
import java.util.Date;

/**
 * @author Hasib Shakur
 * This is the workout class
 * All operations on the workout table are to be done through this class
 */
public class WorkoutDTO {
	private long id;
	private long profileId;
	private Date workoutDate;
	private Time workoutStart;
	private Time workoutEnd;
	private int highHeartRate;
	private int lowHeartRate;
	private double burnedCalories;
	private double timeWithinRange;
	private String workoutType;
	private double averageHeartRate;
	private long timeWithinDesiredRange;
	private long timeWithinAdjustedRange;

	
	public long getTimeWithinDesiredRange() {
		return timeWithinDesiredRange;
	}
	public void setTimeWithinDesiredRange(long timeWithinDesiredRange) {
		this.timeWithinDesiredRange = timeWithinDesiredRange;
	}
	public long getTimeWithinAdjustedRange() {
		return timeWithinAdjustedRange;
	}
	public void setTimeWithinAdjustedRange(long timeWithinAdjustedRange) {
		this.timeWithinAdjustedRange = timeWithinAdjustedRange;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getProfileId() {
		return profileId;
	}
	public void setProfileId(long profileId) {
		this.profileId = profileId;
	}
	public Date getWorkoutDate() {
		return workoutDate;
	}
	public void setWorkoutDate(Date workoutDate) {
		this.workoutDate = workoutDate;
	}
	public Time getWorkoutStart() {
		return workoutStart;
	}
	public void setWorkoutStart(Time workoutStart) {
		this.workoutStart = workoutStart;
	}
	public Time getWorkoutEnd() {
		return workoutEnd;
	}
	public void setWorkoutEnd(Time workoutEnd) {
		this.workoutEnd = workoutEnd;
	}
	public int getHighHeartRate() {
		return highHeartRate;
	}
	public void setHighHeartRate(int highHeartRate) {
		this.highHeartRate = highHeartRate;
	}
	public int getLowHeartRate() {
		return lowHeartRate;
	}
	public void setLowHeartRate(int lowHeartRate) {
		this.lowHeartRate = lowHeartRate;
	}
	public double getBurnedCalories() {
		return burnedCalories;
	}
	public void setBurnedCalories(double burnedCalories) {
		this.burnedCalories = burnedCalories;
	}
	public String getWorkoutType() {
		return workoutType;
	}
	public void setWorkoutType(String workoutType) {
		this.workoutType = workoutType;
	}
	public double getAverageHeartRate() {
		return averageHeartRate;
	}
	public void setAverageHeartRate(double averageHeartRate) {
		this.averageHeartRate = averageHeartRate;
	}
	

}
