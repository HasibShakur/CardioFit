/**
 * 
 */
package com.example.myfirstapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.text.format.Time;
import android.util.Log;

/**
 * @author Hasib Shakur
 *
 */
public class Util {
	
	public double calculateBMI(double heightInInch, double weightInInch)
	{
		return (weightInInch/(Math.pow(heightInInch, 2)))*703;
	}
	public static int getMaxHeartRate(int age)
	{
		return (220 - age);
	}
	public static int getHeavyAerobicLowHeartRate(int age)
	{
		return (int) (getMaxHeartRate(age) * .8);
	}
	public static int getHeavyAerobicHighHeartRate(int age)
	{
		return (int) (getMaxHeartRate(age) * .9);
	}
	public static int getLightAerobicLowHeartRate(int age)
	{
		return (int) (getMaxHeartRate(age) * .7);
	}
	public static int getLightAerobicHighHeartRate(int age)
	{
		return (int) (getMaxHeartRate(age) * .8);
	}
	public static int getHeavyWeightManageLowHeartRate(int age)
	{
		return (int) (getMaxHeartRate(age) * .6);
	}
	public static int getHeavyWeightManageHighHeartRate(int age)
	{
		return (int) (getMaxHeartRate(age) * .7);
	}
	public static int getLightWeightManageLowHeartRate(int age)
	{
		return (int) (getMaxHeartRate(age) * .5);
	}
	public static int getLightWeightManageHighHeartRate(int age)
	{
		return (int) (getMaxHeartRate(age) * .6);
	}
	
	
	/**
	 * Used for conversion of the raw data bluetooth packet
	 * @param data the raw data for heart rate
	 * @return hexadecimal representation of heart rate
	 */
  	public static String byteToHex(byte data) {
  		StringBuffer buf = new StringBuffer();
  		buf.append(toHexChar((data >>> 4) & 0x0F));
  		buf.append(toHexChar(data & 0x0F));
  		return buf.toString();
  	}
  	public static char toHexChar(int i) {
  		if ((0 <= i) && (i <= 9))
  			return (char) ('0' + i);
  		else
  			return (char) ('a' + (i - 10));
  	}
  	
  	public static String dateConversion (String dateStr) throws ParseException {
  		DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
  		Date date = (Date)formatter.parse(dateStr);

  		Calendar cal = Calendar.getInstance();
  		cal.setTime(date);
  		String formattedDate = cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" +         cal.get(Calendar.YEAR);
  		return formattedDate;
  	}

    public static int getAverage(ArrayList<Integer> heartRates) {
    	int total = 0;
        for (int hr : heartRates) {
        	total = total + hr;
        }
        int avg = total/heartRates.size();
    	return avg;
    }
    
	public static Double CalculateCalories(String gender, int avgHeartRate, double weight, int age, long duration) {
		if (gender.equals("m")) {
			double calories = ((-55.0969 + (.6309 * (double) avgHeartRate) + (0.1988 * (0.453592 * weight)) + (0.2017 * (double) age)) / (4.184)) * 60.0 * (duration/(1000.0*60.0*60.0));
			return calories;
		} else {
			double calories = ((-20.4022 + (0.4472 * (double) avgHeartRate) - (0.1263 * (0.453592 * weight)) + (0.074 * (double) age))/ 4.184) * 60.0 * (duration/(1000.0*60.0*60.0));
			return calories;
		}
	}
}
