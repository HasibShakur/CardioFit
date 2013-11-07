/**
 * 
 */
package com.example.myfirstapp;

/**
 * @author Hasib Shakur
 *
 */
public class Util {
	
	public double calculateBMI(double heightInInch, double weightInInch)
	{
		return (weightInInch/(Math.pow(heightInInch, 2)))*703;
	}
	public int getMaxHeartRate(int age)
	{
		return (220 - age);
	}
	public int getAerobicLowHeartRate(int age)
	{
		return (int) (getMaxHeartRate(age) * .7);
	}
	public int getAerobicHighHeartRate(int age)
	{
		return (int) (getMaxHeartRate(age) * .8);
	}
	public int getWeightManageLowHeartRate(int age)
	{
		return (int) (getMaxHeartRate(age) * .6);
	}
	public int getWeightManageHighHeartRate(int age)
	{
		return (int) (getMaxHeartRate(age) * .7);
	}

}
