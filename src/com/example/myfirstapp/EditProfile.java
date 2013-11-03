package com.example.myfirstapp;

import java.util.ArrayList;

import com.example.DBConnection.DBOperateDAO;
import com.example.DBConnection.ProfileDTO;

import android.os.Bundle;
import android.app.Activity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.SuppressLint;
import android.content.Intent;

public class EditProfile extends Activity {
	//create the DAO class reference 
	private DBOperateDAO operatorDao;
	private EditText nameText, ageText, weightText, heightFtText, heightInText, heartRateHighText, heartRateLowText;
	private Button saveButton;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_profile);
		Intent intent = getIntent();
		//create the DAO class object here
		operatorDao = new DBOperateDAO(this);
		//open Database connection
		operatorDao.openDatabase();
		
		//Get the items from view 
		nameText = (EditText) findViewById(R.id.editTextName);
		ageText = (EditText) findViewById(R.id.editTextAge);
		weightText = (EditText) findViewById(R.id.editTextWeight);
		heightFtText = (EditText) findViewById(R.id.editTextHeightFt);
		heightInText = (EditText) findViewById(R.id.editTextHeightIn);
		heartRateHighText = (EditText) findViewById(R.id.editTextHeartRateHigh);
		heartRateLowText = (EditText) findViewById(R.id.editTextHeartRateLow);
		saveButton = (Button) findViewById(R.id.buttonSaveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// do the saving operation here
				String userName = nameText.getText().toString();
				String age = ageText.getText().toString();
				String weight = weightText.getText().toString();
				String heightFt = heightFtText.getText().toString();
				String heightIn = heightInText.getText().toString();
				String heartRateHigh = heartRateHighText.getText().toString();
				String heartRateLow = heartRateLowText.getText().toString();
				ProfileDTO profile  = new ProfileDTO();
				if(userName == null || userName.isEmpty())
				{
					Toast.makeText(getApplicationContext(), "Name not provided", Toast.LENGTH_LONG).show(); 
					return;
				}
				if(age == null || age.isEmpty())
				{
					Toast.makeText(getApplicationContext(), "Age not provided", Toast.LENGTH_LONG).show(); 
					return;
				}
				if(weight == null || weight.isEmpty())
				{
					Toast.makeText(getApplicationContext(), "Weight not provided", Toast.LENGTH_LONG).show(); 
					return;
				}
				if((heightFt == null || heightFt.isEmpty()) && (heightIn == null || heightIn.isEmpty()))
				{
					Toast.makeText(getApplicationContext(), "Height is provided", Toast.LENGTH_LONG).show(); 
					return;
				}
				if(heartRateHigh == null || heartRateHigh.isEmpty())
				{
					Toast.makeText(getApplicationContext(), "High Heart Rate  not provided", Toast.LENGTH_LONG).show(); 
					return;
				}
				if(heartRateLow == null || heartRateLow.isEmpty())
				{
					Toast.makeText(getApplicationContext(), "Low Heart Rate  not provided", Toast.LENGTH_LONG).show(); 
					return;
				}
				profile.setPersonName(userName.trim());
				profile.setPersonAge(Integer.parseInt(age.trim()));
				profile.setWeight(Double.parseDouble(weight.trim()));
				double ft = Double.parseDouble(heightFt.trim());
				double in = Double.parseDouble(heightIn.trim());
				profile.setHeight(((ft*12)+in)*0.0254);
				int heartHigh = Integer.parseInt(heartRateHigh.trim());
				int heartLow = Integer.parseInt(heartRateLow.trim());
				profile.setWeightManageHighHeartRate(heartHigh);
				profile.setWeightManageLowHeartRate(heartLow);
				profile.setAerobicHighHeartRate(heartHigh);
				profile.setAerobicLowHeartRate(heartLow);
				
				operatorDao.createProfile(profile);
				Toast.makeText(getApplicationContext(), "Profile Created Successfully", Toast.LENGTH_LONG).show(); 
				
				//test
				ArrayList<ProfileDTO> test = new ArrayList<ProfileDTO>();
				test = operatorDao.getAllProfiles();
				System.out.println(test.size());
				for(int i=0;i<test.size();i++)
				{
					System.out.println(test.get(i).getPersonId()+ "     "+test.get(i).getPersonName());
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		operatorDao.closeDatabase();
	}

}
