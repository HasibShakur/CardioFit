package com.example.myfirstapp;

import java.util.ArrayList;

import com.example.DBConnection.DBOperateDAO;
import com.example.DBConnection.ProfileDTO;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.SuppressLint;
import android.content.Intent;

public class EditProfile extends Activity {
	private static final String TAG = "EditProfile";
	//create the DAO class reference 
	private DBOperateDAO operatorDao;
	private EditText nameText, ageText, weightText, heightFtText, heightInText;
	private Button saveButton;
	private RadioGroup radioGroup;
	private Util util = new Util();
	
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

		//Get the items from view & set their initial values (if they exist)
		ArrayList<ProfileDTO> profiles = new ArrayList<ProfileDTO>();
		profiles = operatorDao.getAllProfiles();

		nameText = (EditText) findViewById(R.id.editTextName);
		ageText = (EditText) findViewById(R.id.editTextAge);
		weightText = (EditText) findViewById(R.id.editTextWeight);
		heightFtText = (EditText) findViewById(R.id.editTextHeightFt);
		heightInText = (EditText) findViewById(R.id.editTextHeightIn);
		
		Log.i(TAG, "" + profiles.size());
		
		if (!(profiles.size() < 1)) {
			nameText.setText(profiles.get(0).getPersonName());
			ageText.setText("" + profiles.get(0).getPersonAge());
			weightText.setText("" +profiles.get(0).getWeight());
			
			double height = profiles.get(0).getHeight();
			double height_ft = height/12;
			int height_ft_int = (int) height_ft;
			double height_in = height % 12;
			int height_in_int = (int) height_in;
					
			heightFtText.setText("" + height_ft_int);
			heightInText.setText("" + height_in_int);
			
		}
		
		saveButton = (Button) findViewById(R.id.buttonSaveButton);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
        saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// do the saving operation here
				
				String userName = nameText.getText().toString();
				String age = ageText.getText().toString();
				String weight = weightText.getText().toString();
				String heightFt = heightFtText.getText().toString();
				String heightIn = heightInText.getText().toString();
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
				profile.setPersonName(userName.trim());
				profile.setPersonAge(Integer.parseInt(age.trim()));
				profile.setWeight(Double.parseDouble(weight.trim()));
				double ft = Double.parseDouble(heightFt.trim());
				double in = Double.parseDouble(heightIn.trim());
				profile.setHeight((ft*12)+in);
				String gender = "";
				int selectedOption = radioGroup.getCheckedRadioButtonId();
				if(selectedOption == R.id.radio_female)
				{
					gender = "f";
				}
				else if(selectedOption == R.id.radio_male)
				{
					gender = "m";
				}
				profile.setGender(gender);
				ArrayList<ProfileDTO> profiles = new ArrayList<ProfileDTO>();
				profiles = operatorDao.getAllProfiles();
				for(int i = 0;i<profiles.size();i++)
				{
					System.out.println(profiles.get(i).getPersonName() + "...."+ profiles.get(i).getGender());
				}
				if (!(profiles.size() < 1)) {
					operatorDao.updateProfile(profile);
					Toast.makeText(getApplicationContext(), "Profile Updated Successfully", Toast.LENGTH_LONG).show(); 
				} else {
					operatorDao.createProfile(profile);
					Toast.makeText(getApplicationContext(), "Profile Created Successfully", Toast.LENGTH_LONG).show(); 
				}
				
			}

//			private String getGenderFromUI(View v) {
//				String gender  ="m";
//				boolean checked = ((RadioButton) v).isChecked();
//				switch(v.getId()){
//				case R.id.radio_male:
//				if(checked)
//				{
//					gender = "m";
//				}
//				break;
//				case R.id.radio_female:
//				if(checked)
//				{
//					gender = "f";
//				}
//				break;
//			}
//				return gender;
//			}
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
