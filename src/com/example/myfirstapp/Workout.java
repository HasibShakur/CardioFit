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

import java.util.ArrayList;

import com.example.DBConnection.DBOperateDAO;
import com.example.DBConnection.ProfileDTO;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

import com.example.DBConnection.DBOperateDAO;
import com.example.DBConnection.ProfileDTO;
import com.example.DBConnection.WorkoutDTO;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
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

/**
 * This is the main Activity that displays the current chat session.
 */
public class Workout extends Activity implements OnInitListener {
	
    private TextToSpeech tts;
    private static boolean voice_on = true;
    private static final int VOICE_OFF = 9;
    private static final int VOICE_ON = 8;
    private static final int secure_connect_scan = 7;

	 // Debugging
    private static final String TAG = "Workout";
    private static final boolean D = true;

    
    //Timer
    private Chronometer mChronometer;
    long timeWhenClicked = 0;
    long startTime = 0;
    long endTime = 0;
    boolean isChronometerRunning = false;

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
    private ArrayList<Integer> heartRates = new ArrayList<Integer>();
    private ArrayList<Integer> tempHeartRates = new ArrayList<Integer>();
    public static ArrayList<Integer> avgTempHeartRates = new ArrayList<Integer>();
    public static int heart_range_low;
    public static int heart_range_high;
    public static int current_range_low;
    public static int current_range_high;
    public static int consecutive_lows;
    public static int consecutive_highs;
    
    private NotificationManager mNM;
    private static boolean service_is_running = false;

    private ArrayList<ProfileDTO> profiles = new ArrayList<ProfileDTO>();
    WorkoutDTO workout = new WorkoutDTO();

    
    

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "[ACTIVITY] onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.workout);
		
		// Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
                
        Intent intent = getIntent();
        String workout_type = intent.getStringExtra(StartWorkout.WORKOUT_TYPE);
        String heartRange_type = intent.getStringExtra(StartWorkout.HEART_RANGE_TYPE);
        
        mWorkoutType = (TextView) findViewById(R.id.workout_type);
        mWorkoutType.setText(workout_type);
        
        //create the DAO class object here
		operatorDao = new DBOperateDAO(this);
		//open Database connection
		operatorDao.openDatabase();
		
		//Get the items from view & set their initial values (if they exist)
		ArrayList<ProfileDTO> profiles = new ArrayList<ProfileDTO>();
		//Get the items from view & set their initial values (if they exist)

		profiles = operatorDao.getAllProfiles();
		
		if ((profiles.size() < 1)) {
			Intent edit_profile_intent = new Intent(this, EditProfile.class);
			startActivity(edit_profile_intent);
		} else {
	        mHeartRange = (TextView) findViewById(R.id.heart_range_value);
	        mAdjustedHeartRange = (TextView) findViewById(R.id.adjusted_heart_range_value);
	     // Where previous aerobinc and other type of workout related heart rate data was stored
//	        if (heartRange_type.equals("Aerobic")){
//	        	heart_range_low = profiles.get(0).getAerobicLowHeartRate();
//		        heart_range_high = profiles.get(0).getAerobicHighHeartRate();
//	        } else {
//	        	heart_range_low = profiles.get(0).getWeightManageLowHeartRate();
//		        heart_range_high = profiles.get(0).getWeightManageHighHeartRate();
//	        }
	        current_range_low = heart_range_low;
	        current_range_high = heart_range_high;
	        
	        mHeartRange.setText(heart_range_low + " - " + heart_range_high);
	        mAdjustedHeartRange.setText(heart_range_low + " - " + heart_range_low);
		}
        

	}

	@Override
    protected void onStart() {
        Log.i(TAG, "[ACTIVITY] onStart");
        super.onStart();
        
        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
        	Log.i(TAG, "service_is_running = " + service_is_running);
            if (mChatService == null && service_is_running==false) setupChat();
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "[ACTIVITY] onResume");
        super.onResume();
        
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
              // Start the Bluetooth chat services
              mChatService.start();
            }
        }        
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
        if (mChatService != null) mChatService.stop();
        
        //tts.shutdown();

    }
    
    protected void onRestart() {
        Log.i(TAG, "[ACTIVITY] onRestart");
        super.onRestart();
    }
    
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
        
    	new StringBuffer("");
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
    }
    
    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mHeartRate = (TextView) findViewById(R.id.heart_rate);
            mChronometer = (Chronometer) findViewById(R.id.chronometer);

            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                    mConversationArrayAdapter.clear();
                    mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    showNotification();
                    service_is_running = true;
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    setStatus(R.string.title_connecting);
                    mHeartRate.setText("");
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    setStatus(R.string.title_not_connected);       
                    mHeartRate.setText("Please connect heart monitor");
                    if (service_is_running) {
                    	mNM.cancel(R.string.app_name);
                    	service_is_running = false;
                    }
                    break;
                }
                break;
            case MESSAGE_READ:
            	byte[] readBuf = null;
            	readBuf = (byte[]) msg.obj;  
                String Value = Util.byteToHex(readBuf[13]);
                int heart_rate = Integer.parseInt(Value, 16);
                
                //Ignore non-sensible data
                if (heart_rate < 50 || heart_rate > 220) {
                	break;
                }
                
                //Add the heart rate to an array to be used for calculating average heart rate
                heartRates.add(heart_rate);
                tempHeartRates.add(heart_rate);
                
                //Calculate average of last 15 heart rates
                int avg = 0;
                if (tempHeartRates.size() == 15) {
                	avg = getAverage(tempHeartRates);
                    avgTempHeartRates.add(avg);
            		tempHeartRates.clear();
                }
                
                if (avg >= current_range_low && avg <= current_range_high) {
                	if (current_range_low == heart_range_low && current_range_high == heart_range_high) {
                		consecutive_lows = 0;
                		consecutive_highs = 0;
                	} else if(avg < heart_range_low) {
                		consecutive_lows += 1;
                	} else {
                		consecutive_highs += 1;
                	}
                	if (consecutive_lows == 15) {
                		int difference = heart_range_low - current_range_low;
                		if (difference > 5) {
                			current_range_low += 5;
                			current_range_high += 5;
                		} else {
                			current_range_low = heart_range_low;
                			current_range_high = heart_range_high;
                		}
                	} else if (consecutive_highs == 15) {
                		int difference = current_range_high - heart_range_high;
                		if (difference > 5) {
                			current_range_high -= 5;
                			current_range_low -= 5;
                		} else {
                			current_range_high = heart_range_high;
                			current_range_low = heart_range_low;
                		}
                	}
                	
                	if (consecutive_lows == 15 || consecutive_highs == 15) {
                		//adjust heart_ranges
                		int avg_of_avg = getAverage(avgTempHeartRates);
                		Log.i(TAG, "Avg of avg = " + avg_of_avg);
                		avgTempHeartRates.clear();
                		current_range_low = avg_of_avg - 10;
                		current_range_high = avg_of_avg + 10;
                		consecutive_lows = consecutive_highs = 0;
                		if (voice_on) tts.speak("Suggested Heart Range Adjusted", TextToSpeech.QUEUE_ADD, null);
                	} 
                } else {
                	if (avg < current_range_low && avg != 0) {
                		consecutive_lows += 1;
                	} else {
                		consecutive_highs += 1;
                	}
                	
                	if (consecutive_lows == 15 || consecutive_highs == 15) {
                		//adjust heart_ranges
                		int avg_of_avg = getAverage(avgTempHeartRates);
                		Log.i(TAG, "Avg of avg = " + avg_of_avg);
                		avgTempHeartRates.clear();
                		current_range_low = avg_of_avg - 10;
                		current_range_high = avg_of_avg + 10;
                		consecutive_lows = consecutive_highs = 0;
                		if (voice_on) tts.speak("Suggested Heart Range Adjusted", TextToSpeech.QUEUE_ADD, null);
                	}
                }
                                
                if (avg <= current_range_low && avg != 0 && voice_on == true) {
            		tts.speak("Heart Rate Too Low", TextToSpeech.QUEUE_ADD, null);
                }
                if (avg >= current_range_high && voice_on == true) {
            		tts.speak("Heart Rate Too High", TextToSpeech.QUEUE_ADD, null);
                }
                
                if (heart_rate <= current_range_low) {
                	mHeartRate.setTextColor(Color.parseColor("#33B5E5"));
                } else if (heart_rate >= current_range_high) {
                	mHeartRate.setTextColor(Color.parseColor("#FF4444"));
                } else {
                	mHeartRate.setTextColor(Color.parseColor("#99CC00"));
                }
                mHeartRate.setText(String.valueOf(heart_rate));
                
    	        mAdjustedHeartRange = (TextView) findViewById(R.id.adjusted_heart_range_value);
    	        mAdjustedHeartRange.setText(current_range_low + " - " + current_range_high);

                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();

                //Start the clock
                if (!isChronometerRunning) {
        			mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenClicked);
        			timeWhenClicked = mChronometer.getBase() - SystemClock.elapsedRealtime();
        			mChronometer.start();
        			startTime = System.currentTimeMillis(); 
        			isChronometerRunning = true;
        		}
                
        		if (tts != null && voice_on == true) {
	        		tts.speak("Workout Started", TextToSpeech.QUEUE_ADD, null);
        		}
        		
        	    Intent checkIntent = new Intent();
        	    checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        	    startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
        	    
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    

    public int getAverage(ArrayList<Integer> heartRates) {
    	int total = 0;
        for (int hr : heartRates) {
        	total = total + hr;
        }
        int avg = total/heartRates.size();
    	return avg;
    }

    
    
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
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

    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, true);
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
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        case VOICE_OFF:
        	voice_on = false;
            Toast.makeText(this, R.string.voice_turned_off, Toast.LENGTH_SHORT).show();
        	return true;
        case VOICE_ON:
        	voice_on = true;
            Toast.makeText(this, R.string.voice_turned_on, Toast.LENGTH_SHORT).show();
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
        return true;
    }
    
	@Override
	public void onInit(int status) {
		Log.i(TAG, "onInit");
        if (status == TextToSpeech.SUCCESS) {
            Toast.makeText(Workout.this, "Text-To-Speech engine is initialized", Toast.LENGTH_LONG).show();
            if (voice_on == true) {
            	tts.speak("Workout Started", TextToSpeech.QUEUE_ADD, null);
            }
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
    	
    	//If the heart monitor was never attached --> can't save any information
    	if (heartRates.size() == 0) {
    		Toast.makeText(getApplicationContext(), "No workout data to save", Toast.LENGTH_LONG).show(); 
    	}
		endTime = System.currentTimeMillis();
		
		profiles = operatorDao.getAllProfiles();
		workout.setProfileId(profiles.get(0).getPersonId());
		workout.setWorkoutDate(Calendar.getInstance().getTime());
		workout.setWorkoutStart(new Time(startTime));
		workout.setWorkoutEnd(new Time(endTime));
		workout.setWorkoutType(mWorkoutType.getText().toString().trim());
		workout.setHighHeartRate(heart_range_high);
		workout.setLowHeartRate(heart_range_low);
		
		// for just now these are set to 0.0
		workout.setBurnedCalories(0.0);
		workout.setTimeWithinRange(0.0);
		operatorDao.CreateWorkout(workout);
		Toast.makeText(getApplicationContext(), "Workout Data Saved Successfully", Toast.LENGTH_LONG).show(); 
		
	}
	
	public void discardWorkout (View view) {
		//TODO: method to discard data and return to previous screen
		//operatorDao.deleteWorkout(workout);
		//Toast.makeText(getApplicationContext(), "Workout Data Deleted Successfully", Toast.LENGTH_LONG).show(); 	
		
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

}
