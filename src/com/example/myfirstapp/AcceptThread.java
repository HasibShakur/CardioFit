package com.example.myfirstapp;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
	static UUID MY_UUID = UUID.randomUUID();
	String NAME = "CardioFit";
 
    public AcceptThread() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) { }
        mmServerSocket = tmp;
    }
 
    public void run() {
        BluetoothSocket socket = null;
        Log.i("ACCEPT", "entered run");
        // Keep listening until exception occurs or a socket is returned
        while (true) {
        	Log.i("ACCEPT", "listening...");
        	try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // Do work to manage the connection (in a separate thread)
            	Log.i("ACCEPT", "accepted a connection");
                //manageConnectedSocket(socket);
                try {
					mmServerSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
                break;
            }
        }
    }
 
    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) { }
    }
}