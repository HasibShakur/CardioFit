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
    
    
    private ArrayList<Integer> heartRates = new ArrayList<Integer>();
    private ArrayList<Integer> tempHeartRates = new ArrayList<Integer>();
    public static int heart_range_low;
    public static int heart_range_high;
    
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
        
        mWorkoutType = (TextView) findViewById(R.id.workout_type);
        mWorkoutType.setText(workout_type);
        
        //create the DAO class object here
		operatorDao = new DBOperateDAO(this);
		//open Database connection
		operatorDao.openDatabase();
		
		//Get the items from view & set their initial values (if they exist)
		ArrayList<ProfileDTO> profiles = new ArrayList<ProfileDTO>();
		profiles = operatorDao.getAllProfiles();
		
		if ((profiles.size() < 1)) {
			Intent edit_profile_intent = new Intent(this, EditProfile.class);
			startActivity(edit_profile_intent);
		} else {
	        mHeartRange = (TextView) findViewById(R.id.heart_range_value);
	        heart_range_low = profiles.get(0).getAerobicLowHeartRate();
	        heart_range_high = profiles.get(0).getAerobicHighHeartRate();
	        mHeartRange.setText(heart_range_low + " - " + heart_range_high);
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
        super.onDestroy();
        
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        tts.shutdown();

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
            	
            	//String Value = byteToHex(readBuf[1]);
            	//String Value2 = byte2hex(readBuf);
                String Value = parseBioharnessPacket(readBuf);
            	Log.i(TAG, Value);
                int heart_rate = Integer.parseInt(Value, 16);
                
                
                
                // Need to change to fit user's range for whichever workout they chose
                // TEMPORARY
                heartRates.add(heart_rate);

                tempHeartRates.add(heart_rate);
                int avg = 0;
                if (tempHeartRates.size() == 15) {
                	int total = 0;
	                for (int hr : tempHeartRates) {
	                	total = total + hr;
	                }
	                avg = total/15;
	                Log.i(TAG, "AVERAGE = " + avg);
            		tempHeartRates.clear();

                }
                
                if (avg <= heart_range_low && avg != 0 && voice_on == true) {
            		tts.speak("Heart Rate Too Low", TextToSpeech.QUEUE_ADD, null);
                }
                if (avg >= heart_range_high && voice_on == true) {
            		tts.speak("Heart Rate Too High", TextToSpeech.QUEUE_ADD, null);
                }
                
                if (heart_rate <= heart_range_low) {
                	mHeartRate.setTextColor(Color.parseColor("#33B5E5"));
                } else if (heart_rate >= heart_range_high) {
                	mHeartRate.setTextColor(Color.parseColor("#FF4444"));
                } else {
                	mHeartRate.setTextColor(Color.parseColor("#99CC00"));
                }
                mHeartRate.setText(String.valueOf(heart_rate));
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
        menu.add(0, secure_connect_scan, 0, R.string.secure_connect)
        .setIcon(android.R.drawable.ic_menu_search)
        .setShortcut('9', 'q');
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
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /*
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
        super.onDestroy();
        
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        tts.shutdown();

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
            	
            	//String Value = byteToHex(readBuf[1]);
            	//String Value2 = byte2hex(readBuf);
                String Value = parseBioharnessPacket(readBuf);
            	Log.i(TAG, Value);
                int heart_rate = Integer.parseInt(Value, 16);
                
                
                
                // Need to change to fit user's range for whichever workout they chose
                // TEMPORARY
                heartRates.add(heart_rate);

                tempHeartRates.add(heart_rate);
                int avg = 0;
                if (tempHeartRates.size() == 15) {
                	int total = 0;
	                for (int hr : tempHeartRates) {
	                	total = total + hr;
	                }
	                avg = total/15;
	                Log.i(TAG, "AVERAGE = " + avg);
            		tempHeartRates.clear();

                }
                
                if (avg <= heart_range_low && avg != 0 && voice_on == true) {
            		tts.speak("Heart Rate Too Low", TextToSpeech.QUEUE_ADD, null);
                }
                if (avg >= heart_range_high && voice_on == true) {
            		tts.speak("Heart Rate Too High", TextToSpeech.QUEUE_ADD, null);
                }
                
                if (heart_rate <= heart_range_low) {
                	mHeartRate.setTextColor(Color.parseColor("#33B5E5"));
                } else if (heart_rate >= heart_range_high) {
                	mHeartRate.setTextColor(Color.parseColor("#FF4444"));
                } else {
                	mHeartRate.setTextColor(Color.parseColor("#99CC00"));
                }
                mHeartRate.setText(String.valueOf(heart_rate));
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
        menu.add(0, secure_connect_scan, 0, R.string.secure_connect)
        .setIcon(android.R.drawable.ic_menu_search)
        .setShortcut('9', 'q');
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
	
	*/
   public void playMusic(View view) {
		@SuppressWarnings("deprecation")
		Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
		startActivity(intent);
	}
	
	public void startTimer(View view) {
		if (!isChronometerRunning) {
			mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenClicked);
			timeWhenClicked = mChronometer.getBase() - SystemClock.elapsedRealtime();
			mChronometer.start();
			startTime = System.currentTimeMillis(); 
			isChronometerRunning = true;
            if (voice_on == true) {
            	tts.speak("Timer Started", TextToSpeech.QUEUE_ADD, null);
            }

		}
	}
	
	public void stopTimer (View view) {
		if (isChronometerRunning) {
			timeWhenClicked = mChronometer.getBase() - SystemClock.elapsedRealtime();
			mChronometer.stop();
			endTime = System.currentTimeMillis();
			isChronometerRunning = false;
            if (voice_on == true) {
            	tts.speak("Timer Stopped", TextToSpeech.QUEUE_ADD, null);
            }
		}
	}
	
	public void resetTimer (View view) {
		mChronometer.setBase(SystemClock.elapsedRealtime());
		if (isChronometerRunning) {
			mChronometer.stop();
			isChronometerRunning = false;
		}
		timeWhenClicked = 0;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
    public void saveWorkout (View view) {
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
		workout.setDistance(0.0);
		operatorDao.CreateWorkout(workout);
		Toast.makeText(getApplicationContext(), "Workout Data Saved Successfully", Toast.LENGTH_LONG).show(); 
	}
	
	public void discardWorkout (View view) {
		//TODO: method to discard data and return to previous screen
		operatorDao.deleteWorkout(workout);
		Toast.makeText(getApplicationContext(), "Workout Data Deleted Successfully", Toast.LENGTH_LONG).show(); 	
	}
	public String byte2hex(byte[] b){
        // String Buffer can be used instead
        String hs = "";
        String stmp = "";

        for (int n = 0; n < b.length; n++){
           stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));

           if (stmp.length() == 1){
              hs = hs + "0" + stmp;
           }
           else{
              hs = hs + stmp;
           }
           if (n < b.length - 1){
              hs = hs + "";
           }
        }

        return hs;
     }
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
  	public static int merge(byte low, byte high) {
  		int b = 0;
  		b += (high << 8) + low;
  		if ((high & 0x80) != 0) {
  			b = -(0xffffffff - b);
  		}
  		return b;
  	}
  	public static String parseBioharnessPacket(byte[] packet) {

  		String hrValue = null;
  		String hrBytes = null;
  		String batteryValue = null;
  		String postureValue = null;
  		String respirationValue = null;
  		String tempValue = null;
  		
  		try {

  			/** add packet type to avoid confusion with RR packets */
  			// command.add(constants.KIND, DATA);
  			//command.add(PrototypeFactory.beat, ZephyrUtils.parseString(packet, 3));
  			
  			hrBytes = byteToHex(packet[13]);
  			//short hrtValue = Short.parseShort(hrBytes, 16);
  			//command.add(PrototypeFactory.heart, Short.toString(hrValue));
  			//hrValue = String.valueOf(hrtValue);
  			//Log.i(TAG, "hrValue = " + hrValue);
  			
  			int v = merge(packet[24], packet[25]);
  			//command.add(PrototypeFactory.battery, String.valueOf(((double) v / (double) 1000)));
  			batteryValue = String.valueOf(((double) v / (double) 1000));
  			//Log.i(TAG, "batteryValue = " + batteryValue);

  			int p = merge(packet[18], packet[19]);
  			//command.add(PrototypeFactory.posture, String.valueOf(((double) p / (double) 10)));
  			postureValue = String.valueOf(((double) p / (double) 10));
  			//Log.i(TAG, "posture value = " + postureValue);
  			
  			int r = merge(packet[14], packet[15]);
  			//command.add(PrototypeFactory.respiration, String.valueof(Math.abs(((double) r / (double) 10))));
  			respirationValue = String.valueOf(Math.abs(((double) r / (double) 10)));
  			//Log.i(TAG, "respiration value = " + respirationValue);
  			
  			int t = merge(packet[16], packet[17]);
  			//command.add(PrototypeFactory.temperature, String.valueOf(((double) t / (double) 10)));
  			tempValue = String.valueOf(String.valueOf(((double) t / (double) 10)));
  			//Log.i(TAG, "tempValue = " + tempValue);

  		} catch (Exception e) {
  			Log.i(TAG, "parseBioharnessPacket() : " + e.getMessage());
  		}

  		/** add other tags before sending ? */
  		return hrBytes;
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
