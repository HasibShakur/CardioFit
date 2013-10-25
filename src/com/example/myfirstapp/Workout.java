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

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
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
public class Workout extends Activity {
	
	 // Debugging
    private static final String TAG = "Workout";
    private static final boolean D = true;

    
    //Timer
    private Chronometer mChronometer;
    long timeWhenClicked = 0;
    boolean isChronometerRunning = false;

    
    private TextView mWorkoutType;



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

    private StringBuffer mOutStringBuffer;
    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    private TextView mHeartRate;
    private TextView mHeartRange;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    
    

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
	}

	@Override
    protected void onStart() {
        Log.i(TAG, "[ACTIVITY] onStart");
        super.onStart();
        
     // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
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
    }
    
    protected void onRestart() {
        Log.i(TAG, "[ACTIVITY] onRestart");
        super.onDestroy();
    }
    
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
        
    	// Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
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
            mHeartRange = (TextView) findViewById(R.id.heart_range_value);
            mChronometer = (Chronometer) findViewById(R.id.chronometer);

            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                    mConversationArrayAdapter.clear();
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    setStatus(R.string.title_connecting);
                    mHeartRate.setText("");
                    mHeartRange.setText("");
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    setStatus(R.string.title_not_connected);       
                    mHeartRate.setText("Please connect heart monitor");
                    mHeartRange.setText("N/A");
                    break;
                }
                break;
            case MESSAGE_READ:
            	byte[] readBuf = null;
            	readBuf = (byte[]) msg.obj;                
                String Value = parseBioharnessPacket(readBuf);
                int heart_rate = Integer.parseInt(Value, 16);
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
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    
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
    
	/**
	 * Convert a byte to a hex string.
	 * 
	 * @param data
	 *            the byte to convert
	 * @return String the converted byte
	 */
	public static String byteToHex(byte data) {
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data >>> 4) & 0x0F));
		buf.append(toHexChar(data & 0x0F));
		return buf.toString();
	}
	
	/**
	 * Convert an int to a hex char.
	 * 
	 * @param i
	 *            is the int to convert
	 * @return char the converted char
	 */
	public static char toHexChar(int i) {
		if ((0 <= i) && (i <= 9))
			return (char) ('0' + i);
		else
			return (char) ('a' + (i - 10));
	}
	
	/**
	 * Merge two bytes into a signed 2's complement integer
	 * 
	 * @param low
	 *            byte is LSB
	 * @param high
	 *            byte is the MSB
	 * @return a signed intt value
	 */
	public static int merge(byte low, byte high) {
		int b = 0;
		b += (high << 8) + low;
		if ((high & 0x80) != 0) {
			b = -(0xffffffff - b);
		}
		return b;
	}
    
    
    /**
	 * Convert a raw bluetooth packet to XML command object
	 * 
	 * @param packet
	 *            packet is the raw bytes from the SPP
	 * @return Command is the same command but with the Base Bioharness elements
	 *         added
	 */
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
			Log.i(TAG, "hrValue = " + hrValue);
			
			int v = merge(packet[24], packet[25]);
			//command.add(PrototypeFactory.battery, String.valueOf(((double) v / (double) 1000)));
			batteryValue = String.valueOf(((double) v / (double) 1000));
			Log.i(TAG, "batteryValue = " + batteryValue);

			int p = merge(packet[18], packet[19]);
			//command.add(PrototypeFactory.posture, String.valueOf(((double) p / (double) 10)));
			postureValue = String.valueOf(((double) p / (double) 10));
			Log.i(TAG, "posture value = " + postureValue);
			
			int r = merge(packet[14], packet[15]);
			//command.add(PrototypeFactory.respiration, String.valueof(Math.abs(((double) r / (double) 10))));
			respirationValue = String.valueOf(Math.abs(((double) r / (double) 10)));
			Log.i(TAG, "respiration value = " + respirationValue);
			
			int t = merge(packet[16], packet[17]);
			//command.add(PrototypeFactory.temperature, String.valueOf(((double) t / (double) 10)));
			tempValue = String.valueOf(String.valueOf(((double) t / (double) 10)));
			Log.i(TAG, "tempValue = " + tempValue);

		} catch (Exception e) {
			Log.i(TAG, "parseBioharnessPacket() : " + e.getMessage());
		}

		/** add other tags before sending ? */
		return hrBytes;
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
        case R.id.secure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        }
        return false;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
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
			isChronometerRunning = true;
		}
	}
	
	public void stopTimer (View view) {
		if (isChronometerRunning) {
			timeWhenClicked = mChronometer.getBase() - SystemClock.elapsedRealtime();
			mChronometer.stop();
			isChronometerRunning = false;
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

}
