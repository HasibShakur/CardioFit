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

}
