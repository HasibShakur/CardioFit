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

}
