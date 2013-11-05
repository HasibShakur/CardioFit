/**
 * 
 */
package com.example.DBConnection;

/**
 * @author Hasib Shakur
 * This is the profile class
 * All operations on profile table are to be done through this class
 */
public class ProfileDTO {
	private long personId;
	private String personName;
	private int personAge;
	private double weight;
	private double height;
	private int weightManageHighHeartRate;
	private int weightManageLowHeartRate;
	private int aerobicHighHeartRate;
	private int aerobicLowHeartRate;
	public long getPersonId() {
		return personId;
	}
	public void setPersonId(long personId) {
		this.personId = personId;
	}
	public String getPersonName() {
		return personName;
	}
	public void setPersonName(String personName) {
		this.personName = personName;
	}
	public int getPersonAge() {
		return personAge;
	}
	public void setPersonAge(int personAge) {
		this.personAge = personAge;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public double getHeight() {
		return height;
	}
	public void setHeight(double height) {
		this.height = height;
	}
	public int getWeightManageHighHeartRate() {
		return weightManageHighHeartRate;
	}
	public void setWeightManageHighHeartRate(int weightManageHighHeartRate) {
		this.weightManageHighHeartRate = weightManageHighHeartRate;
	}
	public int getWeightManageLowHeartRate() {
		return weightManageLowHeartRate;
	}
	public void setWeightManageLowHeartRate(int weightManageLowHeartRate) {
		this.weightManageLowHeartRate = weightManageLowHeartRate;
	}
	public int getAerobicHighHeartRate() {
		return aerobicHighHeartRate;
	}
	public void setAerobicHighHeartRate(int aerobicHighHeartRate) {
		this.aerobicHighHeartRate = aerobicHighHeartRate;
	}
	public int getAerobicLowHeartRate() {
		return aerobicLowHeartRate;
	}
	public void setAerobicLowHeartRate(int aerobicLowHeartRate) {
		this.aerobicLowHeartRate = aerobicLowHeartRate;
	}
	
	
	
	

}
