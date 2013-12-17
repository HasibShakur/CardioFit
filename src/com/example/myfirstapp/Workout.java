/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myfirstapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import zephyr.android.BioHarnessBT.BTClient;
import zephyr.android.BioHarnessBT.ZephyrProtocol;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.example.DBConnection.DBOperateDAO;
import com.example.DBConnection.ProfileDTO;
import com.example.DBConnection.WorkoutDTO;

/**
 * This is the main Activity that displays the current chat session.
 */
public class Workout extends Activity implements OnInitListener {
	
	/**BLUETOOTH STUFF**/
	BTClient _bt;
	ZephyrProtocol _protocol;
	NewConnectedListener _NConnListener;
	private final int HEART_RATE = 0x100;
	private final int RESPIRATION_RATE = 0x101;
	private final int SKIN_TEMPERATURE = 0x102;
	private final int POSTURE = 0x103;
	private final int PEAK_ACCLERATION = 0x104;
	
    private TextToSpeech tts;
    private static boolean voice_on = true;
    private static final int VOICE_OFF = 9;
    private static final int VOICE_ON = 8;
    private static final int secure_connect_scan = 7;
    private static final int SKIP_WARMUP = 6;

	 // Debugging
    private static final String TAG = "Workout";
    private static final boolean D = true;

    
    //Timer
    private Chronometer mChronometer;
    long timeWhenClicked = 0;
    long startTime = 0;
    long endTime = 0;
    boolean isChronometerRunning = false;
    CountDownTimer countdown;

    //TextViews
    private TextView mWorkoutType;
    private TextView mHeartRate;
    private TextView mHeartRange;
    private TextView mAdjustedHeartRange;
    
	private DBOperateDAO operatorDao;


	public final static String WORKOUT_TYPE = "com.example.myfirstapp.MESSAGE";
	
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;

	private static final int MY_DATA_CHECK_CODE = 0;

    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;
    
    /**Variables Used For manipulating heart range**/
    private HeartRateData heartRates = new HeartRateData();
    //private ArrayList<Integer> heartRates = new ArrayList<Integer>();
    private ArrayList<Integer> tempHeartRates = new ArrayList<Integer>();
    public static ArrayList<Integer> avgTempHeartRates = new ArrayList<Integer>();
    public static int heart_range_low;
    public static int heart_range_high;
    public static int current_range_low;
    public static int current_range_high;
    public static int consecutive_desired_lows;
    public static int consecutive_desired_highs;
    public static int consecutive_outrange_lows;
    public static int consecutive_outrange_highs;
    public static int consecutive_inrange_lows;
    public static int consecutive_inrange_highs;
    public static int consecutive_low_threshold;
    public static int consecutive_high_threshold;
    public static int consecutive_errors;
    public static boolean isWarmup;
    public static long TimeWithinDesiredRange = 0;
    public static long TimeWithinAdjustedRange = 0;
    public boolean previously_out = true;
    public boolean previously_out_adjusted = true;
    public static long adjustedStart;
    public static long adjustedEnd;
    public static long desiredStart;
    public static long desiredEnd;
    public static int change_amount = 5;
    
    private NotificationManager mNM;
    private static boolean service_is_running = false;
    public static String current_range_type;

    private ArrayList<ProfileDTO> profiles = new ArrayList<ProfileDTO>();
    WorkoutDTO workout = new WorkoutDTO();
    int age;
    
    

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "[ACTIVITY] onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.workout);
		
		// Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        //Initialize Text-to-Speech
	    Intent checkIntent = new Intent();
	    checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	    startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        /*Sending a message to android that we are going to initiate a pairing request*/
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        /*Registering a new BTBroadcast receiver from the Main Activity context with pairing request event*/
        this.getApplicationContext().registerReceiver(new BTBroadcastReceiver(), filter);
        // Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
        this.getApplicationContext().registerReceiver(new BTBondReceiver(), filter2);	
                
        Intent intent = getIntent();
        String workout_type = intent.getStringExtra(StartWorkout.WORKOUT_TYPE);
        final String heartRange_type = intent.getStringExtra(StartWorkout.HEART_RANGE_TYPE);
        
        mWorkoutType = (TextView) findViewById(R.id.workout_type);
        mWorkoutType.setText(workout_type);
        
        //create the DAO class object here
		operatorDao = new DBOperateDAO(this);
		//open Database connection
		operatorDao.openDatabase();
		
		//Get the items from view & set their initial values (if they exist)
		final ArrayList<ProfileDTO> profiles = operatorDao.getAllProfiles();

		
		if ((profiles.size() < 1)) {
			Intent edit_profile_intent = new Intent(this, EditProfile.class);
			startActivity(edit_profile_intent);
			return;
		} else {
	        mHeartRange = (TextView) findViewById(R.id.heart_range_value);
	        mAdjustedHeartRange = (TextView) findViewById(R.id.adjusted_heart_range_value);
	        age = profiles.get(0).getPersonAge();
			if (heartRange_type.equals("Light Aerobic")){
	        	heart_range_low = Util.getLightAerobicLowHeartRate(age);
		        heart_range_high = Util.getLightAerobicHighHeartRate(age);
		        current_range_type = "Light Aerobic";
			} else if (heartRange_type.equals("Heavy Aerobic")) {
				heart_range_low = Util.getHeavyAerobicLowHeartRate(age);
		        heart_range_high = Util.getHeavyAerobicHighHeartRate(age);
		        current_range_type = "Heavy Aerobic";
			} else if (heartRange_type.equals("Light Weight-Management")) {
				heart_range_low = Util.getLightWeightManageLowHeartRate(age);
		        heart_range_high = Util.getLightWeightManageHighHeartRate(age);
		        current_range_type = "Light Weight-Management";
	        } else {
	        	heart_range_low = Util.getHeavyWeightManageLowHeartRate(age);
		        heart_range_high = Util.getHeavyWeightManageHighHeartRate(age);
		        current_range_type = "Heavy Weight-Management";
	        }

	        current_range_low = heart_range_low;
	        current_range_high = heart_range_high;
	        
	        mHeartRange.setText(heart_range_low + " - " + heart_range_high);
	        mAdjustedHeartRange.setText(heart_range_low + " - " + heart_range_high);
	        
		}
        

	}

	@Override
    protected void onStart() {
        Log.i(TAG, "[ACTIVITY] onStart");
        super.onStart();
        
        profiles = operatorDao.getAllProfiles();
		if ((profiles.size() < 1)) {
			return;
		}
        
        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "[ACTIVITY] onResume");
        super.onResume();      
    }
    
    
    @Override
    protected void onPause() {
        Log.i(TAG, "[ACTIVITY] onPause");
        super.onPause();
    }
    
    @Override
    protected void onStop() {
        Log.i(TAG, "[ACTIVITY] onStop");
        super.onStop();
    }

    protected void onDestroy() {
        Log.i(TAG, "[ACTIVITY] onDestroy");
        operatorDao.closeDatabase();
        super.onDestroy();
        
        // Stop the Bluetooth chat services
        disconnect();
        
        //tts.shutdown();

    }
    
    protected void onRestart() {
        Log.i(TAG, "[ACTIVITY] onRestart");
        super.onRestart();
    }    
    
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
            	Toast.makeText(this, "Bluetooth now enabled", Toast.LENGTH_SHORT).show();
            } else {
                // User did not enable Bluetooth an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            } 
            break;
        case MY_DATA_CHECK_CODE: // For text to speech
        	Log.i(TAG, "my data check code");
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            	Log.i(TAG, "check voice data pass");
                // success, create the TTS instance
                tts = new TextToSpeech(this, this);
            } 
            else {
                // missing data, install it
            	Log.i(TAG, "not check voice data pass");
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case secure_connect_scan:
        	connect();
            return true;
        case VOICE_OFF:
        	voice_on = false;
            Toast.makeText(this, R.string.voice_turned_off, Toast.LENGTH_SHORT).show();
        	return true;
        case VOICE_ON:
        	voice_on = true;
            Toast.makeText(this, R.string.voice_turned_on, Toast.LENGTH_SHORT).show();
            return true;
        case SKIP_WARMUP:
        	isWarmup = false;
        	countdown.cancel();
            mAdjustedHeartRange = (TextView) findViewById(R.id.adjusted_heart_range_value);
            mAdjustedHeartRange.setText(heart_range_low + " - " + heart_range_high);
            if (tts != null && voice_on) tts.speak("Workout Started", TextToSpeech.QUEUE_ADD, null);
        	return true;
        }
        return false;
    }
    
    /* Creates the menu items */
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (voice_on == true) {
	        menu.add(0, VOICE_OFF, 0, R.string.turn_off_voice)
	        .setIcon(android.R.drawable.ic_lock_power_off)
	        .setShortcut('9', 'q');
        } else {
        	menu.add(0, VOICE_ON, 0, R.string.turn_on_voice)
        	.setIcon(android.R.drawable.ic_lock_power_off)
        	.setShortcut('9', 'q');
        }
        if (!service_is_running) {
            menu.add(0, secure_connect_scan, 0, R.string.secure_connect)
            .setIcon(android.R.drawable.ic_menu_search)
            .setShortcut('9', 'q');
        }
        if (isWarmup) {
        	menu.add(0, SKIP_WARMUP, 0, R.string.skip_warmup)
        	.setIcon(android.R.drawable.ic_lock_power_off)
        	.setShortcut('9', 'q');
        }
        return true;
    }
    
	@Override
	public void onInit(int status) {
		Log.i(TAG, "onInit");
        if (status == TextToSpeech.SUCCESS) {
            Toast.makeText(Workout.this, "Text-To-Speech engine is initialized", Toast.LENGTH_LONG).show();
        }
        else if (status == TextToSpeech.ERROR) {
            Toast.makeText(Workout.this, 
                    "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
        }		
	}
	
   public void playMusic(View view) {
		@SuppressWarnings("deprecation")
		Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
		startActivity(intent);
	}	
	
    public void saveWorkout (View view) {
    	new AlertDialog.Builder(this)
    		.setIcon(android.R.drawable.ic_dialog_alert)
    		.setTitle("Save Workout")
    		.setMessage("Are you sure you want to save and end this workout?")
    		.setPositiveButton("Yes", new DialogInterface.OnClickListener()
     {
         @Override
         public void onClick(DialogInterface dialog, int which) {
        	//If the heart monitor was never attached --> can't save any information
         	if (heartRates.heartRates.size() == 0) {
         		Toast.makeText(getApplicationContext(), "No workout data to save", Toast.LENGTH_LONG).show();
         		return;
         	}
         	endTime = System.currentTimeMillis();
            
            profiles = operatorDao.getAllProfiles();
            workout.setProfileId(profiles.get(0).getPersonId());
            workout.setWorkoutDate(Calendar.getInstance().getTime());
            workout.setWorkoutStart(new Time(startTime));
            workout.setWorkoutEnd(new Time(endTime));
            workout.setWorkoutType(mWorkoutType.getText().toString().trim());
            int max = heartRates.heartRates.get(0);
            int min = heartRates.heartRates.get(0);

            
        	File f = new File(Environment.getExternalStorageDirectory() + File.separator + "heart_rates.csv");
        	PrintWriter out = null;
        	try {
        		out = new PrintWriter(new FileOutputStream(f), true);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}            

            for ( int i = 1; i < heartRates.heartRates.size(); i++) {
                if ( heartRates.heartRates.get(i) > max) {
                	max = heartRates.heartRates.get(i);
                }
                if (heartRates.heartRates.get(i) < min) {
                	min = heartRates.heartRates.get(i);
                }
    			out.print(heartRates.timeStamps.get(i));
    			out.print(",");
    			out.print(heartRates.heartRates.get(i));
    			out.println();
            }
            workout.setHighHeartRate(max);
            workout.setLowHeartRate(min);
            
            
            int avgHeartRate = Util.getAverage(heartRates.heartRates);
            double weight = profiles.get(0).getWeight();
            int age = profiles.get(0).getPersonAge();
            long duration = endTime - startTime;
            String gender = profiles.get(0).getGender();
            Double calories = Util.CalculateCalories(gender, avgHeartRate, weight, age, duration); 
            
            Log.i(TAG, "timeWithinAdjustedRange in ms = " + TimeWithinAdjustedRange);
            Log.i(TAG, "timeWithinDesiredRange = " + (TimeWithinDesiredRange));
            Log.i(TAG, "timeWithinAdjustedRange = " + (TimeWithinAdjustedRange / (1000)));
            //TODO: get the desired and adjusted time. Need to force an "out of range" so the time you were in the range when you quit
        	if (!previously_out) {
        	 	desiredEnd = System.currentTimeMillis();
        	 	TimeWithinDesiredRange += desiredEnd - desiredStart;
        	}
        	if (!previously_out_adjusted) {
        		adjustedEnd = System.currentTimeMillis();
        		TimeWithinAdjustedRange += adjustedEnd - adjustedStart;
        	}
        	
        	
            workout.setBurnedCalories(calories);	    	 
            workout.setTimeWithinDesiredRange(TimeWithinDesiredRange);
            workout.setTimeWithinAdjustedRange(TimeWithinAdjustedRange);
            workout.setAverageHeartRate(avgHeartRate);
            operatorDao.CreateWorkout(workout);
            Toast.makeText(getApplicationContext(), "Workout Data Saved Successfully", Toast.LENGTH_LONG).show();     
     		finish();
     		
         }
     })
     .setNegativeButton("No", null)
     .show();
	}
	
	public void discardWorkout (View view) {
		new AlertDialog.Builder(this)
        	.setIcon(android.R.drawable.ic_dialog_alert)
        	.setTitle("Closing Activity")
        	.setMessage("Are you sure you want to discard and end this workout?")
        	.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        	{
        		@Override
        		public void onClick(DialogInterface dialog, int which) {
        			finish();    
        		}

        	})
        	.setNegativeButton("No", null)
        	.show();		
	}
	
	public void upgradeRange (View view) {
		if (current_range_type.equals("Light Aerobic")) {
			//Go to Heavy Aerobic
			heart_range_low = Util.getHeavyAerobicLowHeartRate(profiles.get(0).getPersonAge());
	        heart_range_high = Util.getHeavyAerobicHighHeartRate(profiles.get(0).getPersonAge());
	        current_range_type = "Heavy Aerobic";
		} else if (current_range_type.equals("Heavy Aerobic")) {
			//Toast that you are already at maximum range
            Toast.makeText(getApplicationContext(), "Already at maximum desired range!", Toast.LENGTH_LONG).show(); 
			return;
		} else if (current_range_type.equals("Light Weight-Management")) {
			// go to heavy weight-management
        	heart_range_low = Util.getHeavyWeightManageLowHeartRate(profiles.get(0).getPersonAge());
	        heart_range_high = Util.getHeavyWeightManageHighHeartRate(profiles.get(0).getPersonAge());
	        current_range_type = "Heavy Weight-Management";
		} else if (current_range_type.equals("Heavy Weight-Management")) {
			// go to light aerobic
        	heart_range_low = Util.getLightAerobicLowHeartRate(profiles.get(0).getPersonAge());
	        heart_range_high = Util.getLightAerobicHighHeartRate(profiles.get(0).getPersonAge());
	        current_range_type = "Light Aerobic";
		} else {
            Toast.makeText(getApplicationContext(), "Error trying to upgrade range.", Toast.LENGTH_LONG).show(); 
		}
		//reset all consecutive counters
		consecutive_desired_lows = consecutive_desired_highs = consecutive_inrange_lows = consecutive_inrange_highs = consecutive_desired_highs = consecutive_outrange_highs = consecutive_outrange_lows = 0;
       
		//Change the display
		mHeartRange = (TextView) findViewById(R.id.heart_range_value);
        mHeartRange.setText(heart_range_low + " - " + heart_range_high);
        
		if (tts != null && voice_on) tts.speak("Desired range upgraded to " + current_range_type, TextToSpeech.QUEUE_ADD, null);

		
	}
	
	public void downgradeRange (View view) {
		if (current_range_type.equals("Light Aerobic")) {
			// go to heavy weight-management
        	heart_range_low = Util.getHeavyWeightManageLowHeartRate(profiles.get(0).getPersonAge());
	        heart_range_high = Util.getHeavyWeightManageHighHeartRate(profiles.get(0).getPersonAge());
	        current_range_type = "Heavy Weight-Management";
		} else if (current_range_type.equals("Heavy Aerobic")) {
			// go to light aerobic
        	heart_range_low = Util.getLightAerobicLowHeartRate(profiles.get(0).getPersonAge());
	        heart_range_high = Util.getLightAerobicHighHeartRate(profiles.get(0).getPersonAge());
	        current_range_type = "Light Aerobic";
		} else if (current_range_type.equals("Light Weight-Management")) {
			//Toast that you are already at minimum range
            Toast.makeText(getApplicationContext(), "Already at minimum desired range!", Toast.LENGTH_LONG).show(); 
        	return;
		} else if (current_range_type.equals("Heavy Weight-Management")) {
			// to to light weight-management
			heart_range_low = Util.getLightWeightManageLowHeartRate(profiles.get(0).getPersonAge());
	        heart_range_high = Util.getLightWeightManageHighHeartRate(profiles.get(0).getPersonAge());
	        current_range_type = "Light Weight-Management";
		}
		// reset all consecutive counters
		consecutive_desired_lows = consecutive_desired_highs = consecutive_inrange_lows = consecutive_inrange_highs = consecutive_desired_highs = consecutive_outrange_highs = consecutive_outrange_lows = 0;
		
		//change the display
        mHeartRange = (TextView) findViewById(R.id.heart_range_value);
        mHeartRange.setText(heart_range_low + " - " + heart_range_high);
        
		if (tts != null && voice_on) tts.speak("Desired range downgraded to " + current_range_type, TextToSpeech.QUEUE_ADD, null);

	}
	
  	/**
     * Show a notification while this service is running.
     */
    @SuppressWarnings("deprecation")
	private void showNotification() {
        CharSequence text = getText(R.string.app_name);
        Notification notification = new Notification(R.drawable.heart1, null, System.currentTimeMillis());
        notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        Intent heartrateIntent = new Intent();
        heartrateIntent.setComponent(new ComponentName(this, Workout.class));
        heartrateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, heartrateIntent, 0);
        notification.setLatestEventInfo(this, text, getText(R.string.notification_subtitle), contentIntent);

        mNM.notify(R.string.app_name, notification);
    }
    
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Closing Activity")
            .setMessage("Are you sure you want to close this activity?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	disconnect();
                finish();    
            }

        })
        .setNegativeButton("No", null)
        .show();
    }
    
	private void startWarmUp() {
		if (tts != null && voice_on == true) {
    		tts.speak("Warm up Started", TextToSpeech.QUEUE_ADD, null);
		}
		isWarmup = true;
		service_is_running = true;
        invalidateOptionsMenu();
        mAdjustedHeartRange = (TextView) findViewById(R.id.adjusted_heart_range_value);
        
        
        //Start the Overall clock
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        if (!isChronometerRunning) {
                        mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenClicked);
                        timeWhenClicked = mChronometer.getBase() - SystemClock.elapsedRealtime();
                        mChronometer.start();
                        startTime = System.currentTimeMillis(); 
                        isChronometerRunning = true;
                }
        
        //Also start the countdown
		countdown = new CountDownTimer(300000, 1000) {
		     public void onTick(long millisUntilFinished) {
		    	 int minutes = (int) (millisUntilFinished / (1000 * 60));
		    	 int seconds = (int) ((millisUntilFinished/1000) - (60 * minutes));
		    	 String adjustedSeconds = String.valueOf(seconds);
		    	 if (seconds < 10) {
		    		 adjustedSeconds = "0" + seconds;
		    	 }
		         mAdjustedHeartRange.setText("Warmup: " + minutes + ":" + adjustedSeconds);
		     }

		     public void onFinish() {
		         mAdjustedHeartRange.setText(heart_range_low + " - " + heart_range_high);
             	 if (tts != null && voice_on) tts.speak("Workout Started", TextToSpeech.QUEUE_ADD, null);
		         isWarmup = false;
		     }
		  }.start();
	}
	
	private void disconnect() {
		if (service_is_running) {
			/*This disconnects listener from acting on received messages*/	
			_bt.removeConnectedEventListener(_NConnListener);
			/*Close the communication with the device & throw an exception if failure*/
			_bt.Close();
		}
	}
	
	private void connect() {
		String BhMacID = "00:07:80:9D:8A:E8";
		//String BhMacID = "00:07:80:88:F6:BF";
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		
		if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
            	if (device.getName().startsWith("BH")) {
            		BluetoothDevice btDevice = device;
            		BhMacID = btDevice.getAddress();
                    break;
            	}
            }
		}
		
		//BhMacID = btDevice.getAddress();
		BluetoothDevice Device = mBluetoothAdapter.getRemoteDevice(BhMacID);
		String DeviceName = Device.getName();
		_bt = new BTClient(mBluetoothAdapter, BhMacID);
		_NConnListener = new NewConnectedListener(Newhandler,Newhandler);
		_bt.addConnectedEventListener(_NConnListener);
		
		if(_bt.IsConnected())
		{
			_bt.start();
			startWarmUp();
            Toast.makeText(getApplicationContext(), "Bluetooth connected", Toast.LENGTH_LONG).show(); 
		}
		else
		{
            Toast.makeText(getApplicationContext(), "Error, Bluetooth not connected", Toast.LENGTH_LONG).show(); 

		}
	}
	
	private class BTBondReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle b = intent.getExtras();
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
			Log.d("Bond state", "BOND_STATED = " + device.getBondState());
		}
    }
    private class BTBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("BTIntent", intent.getAction());
			Bundle b = intent.getExtras();
			Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
			Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
			try {
				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
				Method m = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[] {String.class} );
				byte[] pin = (byte[])m.invoke(device, "1234");
				m = device.getClass().getMethod("setPin", new Class [] {pin.getClass()});
				Object result = m.invoke(device, pin);
				Log.d("BTTest", result.toString());
			} catch (SecurityException e1) {
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
    }
    

    final  Handler Newhandler = new Handler(){
    	public void handleMessage(Message msg)
    	{
    		TextView tv;
    		switch (msg.what)
    		{
    		case HEART_RATE:
    			String HeartRatetext = msg.getData().getString("HeartRate");
    			//tv = (EditText)findViewById(R.id.labelHeartRate);
    			System.out.println("Heart Rate Info is "+ HeartRatetext);
    			//if (tv != null)tv.setText(HeartRatetext);
    			int heart_rate = Integer.valueOf(HeartRatetext);
    			runAlgorithm(heart_rate);
    		break;    		
    		}
    	}

    };
    
    
    public void runAlgorithm(int heart_rate) {   
        mHeartRate = (TextView) findViewById(R.id.heart_rate);

        //Ignore non-sensible data
        if (heart_rate < 50 || heart_rate > 220) {
        	consecutive_errors += 1;
        	if (consecutive_errors >= 3) {
        		mHeartRate.setText("Error");
        	}
        	return;
        } else {
        	consecutive_errors = 0;
        }
        
        if (heart_rate < heart_range_low || heart_rate > heart_range_high) {
        	if (!previously_out) {
        	 	desiredEnd = System.currentTimeMillis();
        	 	TimeWithinDesiredRange += desiredEnd - desiredStart;
        	 	previously_out = true;
        	 }
        } else {
        	if (previously_out) {
        		desiredStart = System.currentTimeMillis();
             	previously_out = false;
        	}
        }
        
        if (heart_rate < current_range_low) {
        	mHeartRate.setTextColor(Color.parseColor("#33B5E5"));
        	 if (!previously_out_adjusted) {
        	 	adjustedEnd = System.currentTimeMillis();
        	 	Log.i(TAG, "ADDING TIME TO ADJUSTED!");
        	 	TimeWithinAdjustedRange += adjustedEnd - adjustedStart;
        	 	Log.i("time = " , "" + TimeWithinAdjustedRange/1000);
        	 	previously_out_adjusted = true;
        	 }
        } else if (heart_rate > current_range_high) {
        	mHeartRate.setTextColor(Color.parseColor("#FF4444"));
           	 if (!previously_out_adjusted) {
         	 	adjustedEnd = System.currentTimeMillis();
        	 	Log.i(TAG, "ADDING TIME TO ADJUSTED!");
         	 	TimeWithinAdjustedRange += adjustedEnd - adjustedStart;
         	 	previously_out_adjusted = true;
         	 }
        } else {
        	mHeartRate.setTextColor(Color.parseColor("#99CC00"));
        	if (previously_out_adjusted) {
        		adjustedStart = System.currentTimeMillis();
             	previously_out_adjusted = false;
        	}
        }
        mHeartRate.setText(String.valueOf(heart_rate));
        
        if (isWarmup) {
        	return;
        }
        
        //Add the heart rate to an array to be used for calculating average heart rate
        //heartRates.add(heart_rate);
        heartRates.heartRates.add(heart_rate);
        heartRates.timeStamps.add(System.currentTimeMillis());
        tempHeartRates.add(heart_rate);
        
        //Calculate average of last 15 heart rates
        int avg = 0;
        if (tempHeartRates.size() == 15) {
        	avg = Util.getAverage(tempHeartRates);
            avgTempHeartRates.add(avg);
    		tempHeartRates.clear();
        }
        
        //If the users heart rate is within our current range
        if (avg >= current_range_low && avg <= current_range_high) {
        	consecutive_outrange_lows = 0;
    		consecutive_outrange_highs = 0;
    		//If the current range is equivalent to the desired heart range
    		//PERFECT! this is what we want
        	if (current_range_low == heart_range_low && current_range_high == heart_range_high) {
        		consecutive_inrange_lows = 0;
        		consecutive_inrange_highs = 0;
        	} 
        	//If the heart rate is within the current range, but still lower than the desired range
        	//increase the consecutive desired lows and consecutive in range lows
        	else if(avg < heart_range_low) {
        		consecutive_desired_lows += 1;
        		consecutive_desired_highs = 0;
        		consecutive_inrange_lows += 1;
        		consecutive_inrange_highs = consecutive_desired_highs = consecutive_outrange_highs = consecutive_outrange_lows = 0;
        	}
        	//If the heart rate is within the current range, but still higher than the desired range
        	//increase the consecutive desired highs and consecutive in range highs
        	else if (avg > heart_range_high) {
        		consecutive_desired_highs += 1;
        		consecutive_inrange_highs += 1;
        		consecutive_inrange_lows = consecutive_desired_lows = consecutive_outrange_lows = consecutive_outrange_highs = 0;
        	}
        	//In this case the average was within the original desired range, so set custom range to be that
    		//GREAT! we've worked back to the desired range!
        	else {
        		current_range_low = heart_range_low;
        		current_range_high = heart_range_high;
        		consecutive_desired_lows = consecutive_desired_highs = 0;
        		consecutive_inrange_lows = consecutive_inrange_highs = 0;
        		consecutive_outrange_lows = consecutive_outrange_highs = 0;
        	}
        	//If the user is continually meeting the customized range, but the range is lower than the optimal range
        	//In this case we will slowly increment the current range until it matches the desired range
        	if (consecutive_inrange_lows == 2) {
        		int difference = heart_range_low - current_range_low;
        		if (difference > 5) {
        			current_range_low += 5;
        			current_range_high += 5;
        		} else {
        			current_range_low = heart_range_low;
        			current_range_high = heart_range_high;
        		}
        		consecutive_inrange_highs = consecutive_inrange_lows = 0;
        	//If the user is continually meeting the customized range, but the range is higher than the optimal range
        	//In this case we will slowly decrement the current range until it matches the desired range
        	} else if (consecutive_inrange_highs == 2) {
        		int difference = current_range_high - heart_range_high;
        		if (difference > 5) {
        			current_range_high -= 5;
        			current_range_low -= 5;
        		} else {
        			current_range_high = heart_range_high;
        			current_range_low = heart_range_low;
        		}
        		consecutive_inrange_highs = consecutive_inrange_lows = 0;
        	} 
        } 
        
        
        
        
        
        
        //If the user has heart range greater than the current range, but the current range is lower than desired range
        //In this case, we will simply move the current_range higher because we want to get closer to the desired range
        else if (avg > current_range_high && current_range_high < heart_range_high) {
        	if (avg > heart_range_high) {
        		current_range_low = heart_range_low;
        		current_range_high = heart_range_high;
        	} else {
            	//current_range_high = avg + 10;
            	//current_range_low = avg - 10;
            	int maxHeartRate = Util.getMaxHeartRate(age);
            	current_range_high = (int) (avg + (maxHeartRate * .05));
            	current_range_low = (int) (avg + (maxHeartRate * .05));
            	//TODO: change this to be .05 of HRmax
        	}
        	consecutive_desired_lows += 1;
        	consecutive_inrange_lows = consecutive_inrange_highs = 0;
        	consecutive_outrange_lows = consecutive_outrange_highs = 0;
        } 
        //If the user has heart range lower than the current range, but the current range is higher than the desired range
        //In this case, we will simply move the current range lower, because we want to get closer to the desired range
        else if (avg < current_range_low && current_range_low > heart_range_low && avg != 0) {
        	if (avg < heart_range_low) {
        		current_range_low = heart_range_low;
        		current_range_high = heart_range_high;
        	} else {
        		//current_range_high = avg + 10;
        		//current_range_low = avg - 10;
            	int maxHeartRate = Util.getMaxHeartRate(age);
            	current_range_high = (int) (avg + (maxHeartRate * .05));
            	current_range_low = (int) (avg + (maxHeartRate * .05));
        		//TODO: change this to be .05 of HRmax
        	}
        	consecutive_desired_highs += 1;
        	consecutive_inrange_lows = consecutive_inrange_highs = 0;
        	consecutive_outrange_lows = consecutive_outrange_highs = 0;
        }
        //The user has a heart range lower than the current range, and the current range is lower than the desired range OR
        //The user has a heart range higher than the current range, and the current range is higher than the desired range
        else {
        	consecutive_low_threshold = consecutive_high_threshold = 3;
        	//If the users heart rate is lower than the current range, and lower than the desired range
        	//We will slowly decrement the current range dependent on how far away they are from the range
        	if (avg < current_range_low && avg != 0) {
        		consecutive_desired_lows += 1;
        		consecutive_outrange_lows += 1;
        		consecutive_desired_highs = consecutive_outrange_highs = consecutive_inrange_highs = consecutive_inrange_lows = 0;
        		if (current_range_low - avg > current_range_low * .2) {
        			consecutive_low_threshold = 1;
        			change_amount = 15;
        		} else if (current_range_low - avg > current_range_low * .1) {
        			consecutive_low_threshold = 2;
        			change_amount = 10;
        		} else {
        			change_amount = 5;
        		}
        	} 
        	//If the users heart rate is higher than the current range, and higher than the desired range
        	//We will slowly increment the current range depended on how far they are away from the range
        	else if (avg > current_range_high && avg!= 0){
        		consecutive_desired_highs += 1;
        		consecutive_outrange_highs += 1;
        		consecutive_desired_lows = consecutive_outrange_lows = consecutive_inrange_lows = consecutive_inrange_highs = 0;
        		if (avg - current_range_high > current_range_high * .2) {
        			consecutive_high_threshold = 1;
        			change_amount = 15;
        		} else if (avg - current_range_high> current_range_high * .1) {
        			consecutive_high_threshold = 2;
        			change_amount = 10;
        		} else {
        			change_amount = 5;
        		}
        	}
        	//If the user is failing to meet our customized heart rate --> need to lower it more
        	if (consecutive_outrange_lows >= consecutive_low_threshold) {
    			current_range_low -= change_amount;
    			current_range_high -= change_amount;
        		consecutive_outrange_highs = consecutive_outrange_lows = 0;
        		change_amount = 5;
        	}
        	//If the user is failing to bring heart rate down to customized heart rate --> need to raise range
        	if (consecutive_outrange_highs >= consecutive_high_threshold) {
    			current_range_low += change_amount;
    			current_range_high += change_amount;
    			consecutive_outrange_highs = consecutive_outrange_lows = 0;
    			change_amount = 5;
        	}
        }
        
        
        //If the user is continually below the desired heart range (even if they are within the current range)
        if (consecutive_desired_lows >= 8) {
        	if (tts != null && voice_on) tts.speak("You need to raise your heart rate to optimize your workout", TextToSpeech.QUEUE_ADD, null);
        	consecutive_desired_lows = 0;
        }
        //if the user is continually above the desired heart range (even if they are within the current range)
        if (consecutive_desired_highs >= 8 ){
        	if (tts != null && voice_on) tts.speak("You need to lower your heart rate to optimize your workout", TextToSpeech.QUEUE_ADD, null);
        	consecutive_desired_highs = 0;
        }
                        
        if (avg <= current_range_low && avg != 0 && tts != null && voice_on == true) {
    		tts.speak("Heart Rate Too Low", TextToSpeech.QUEUE_ADD, null);
        }
        if (avg >= current_range_high && tts != null && voice_on == true) {
    		tts.speak("Heart Rate Too High", TextToSpeech.QUEUE_ADD, null);
        }
        
        
        
        mAdjustedHeartRange = (TextView) findViewById(R.id.adjusted_heart_range_value);
        mAdjustedHeartRange.setText(current_range_low + " - " + current_range_high);
    }

}
