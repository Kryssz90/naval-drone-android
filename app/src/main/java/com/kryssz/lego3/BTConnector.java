package com.kryssz.lego3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.UUID;

//import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;

/**
 * Created by Kryssz on 2015.03.03..
 */
public class BTConnector {

    public static final String TAG = "Connector";

    public static final boolean BT_ON = true;
    public static final boolean BT_OFF = false;

    public BluetoothAdapter bluetoothAdapter;
    public BluetoothSocket bluetoothSocket;
    public String address;

    boolean connected = false;

    public BTConnector(String address) {
        this.address = address;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    public void setBluetooth(boolean state) {
        if(state == BTConnector.BT_ON) {
            // Check if bluetooth is off
            if(this.bluetoothAdapter.isEnabled() == false)
            {
                this.bluetoothAdapter.enable();
                while(this.bluetoothAdapter.isEnabled() == false) {

                }
                Log.d(BTConnector.TAG, "Bluetooth turned on");

            }

        }
        // Check if bluetooth is enabled
        else if(state == BTConnector.BT_OFF) {
            // Check if bluetooth is enabled
            if(this.bluetoothAdapter.isEnabled() == true)
            {
                this.bluetoothAdapter.disable();
                while(this.bluetoothAdapter.isEnabled() == true) {

                }
                Log.d(BTConnector.TAG, "Bluetooth turned off");

            }

        }

    }

    public boolean connect() {

        Log.d("BTconn","Conn start");
        connected = false;
        BluetoothDevice nxt = this.bluetoothAdapter.getRemoteDevice(this.address);

        try {
            Log.d("BTconn","create socket");
            this.bluetoothSocket = nxt.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            Log.d("BTconn","connect socket");
            this.bluetoothSocket.connect();
            connected = true;
            Log.d("BTconn","connect end");
        }
        catch (Exception e) {
            connected = false;

        }

        return connected;

    }

    public byte[] readMessage() {
        byte[] message;

        if(this.bluetoothSocket!= null) {
            try {
                InputStream input = this.bluetoothSocket.getInputStream();

                if(input.available() > 2)
                {
                    int length = input.read() + input.read()*256;
                    message = new byte[length];
                    for(int i = 0; i<length; i++) {
                        message[i] = (byte) input.read();
                    }
                    String dec="";
                    for(int i = 0;i<message.length;i++)
                    {
                        dec += "["+String.valueOf(message[i])+"],";
                    }
                    Log.d("Btread",dec);
                }
                else
                {
                    message = new byte[0];
                }
                 Log.d(BTConnector.TAG, "Successfully read message: "+message.length);
                 return  message;
            }
            catch (Exception e) {
                message = null;
                 Log.d(BTConnector.TAG, "Couldn't read message");
                 return new byte[0];
            }
        }
        else {
            message = null;
             Log.d(BTConnector.TAG, "Couldn't read message");

        }

        return message;
    }

    public char[] toCharArray(byte[] be)
    {
        if(be.length % 2 == 1)
        {
            byte[] par = new byte[be.length +1];
            for (int i = 0; i < be.length; i++)
            {
                par[i] = be[i];
            }
            par[be.length] = 0x00;
            be = par;
        }

        int len = be.length;

        char[] c = new char[len/2];
        for(int i = 0; i< len; i+=2)
        {
            c[i/2] = (char) ( be[i]*256 + be[i+1]);
        }
        return  c;
    }

    public void sendMessage(byte[] message) {

        if(connected) {
            try {
                OutputStream output = this.bluetoothSocket.getOutputStream();
                if (output == null) {
                    bluetoothSocket = null;
                    throw new IOException();

                }
                // send message length
                int messageLength = message.length;
                output.write(messageLength);
                output.write(messageLength >> 8);
                output.write(message, 0, message.length);
                output.flush();
            }
            catch (Exception e)
            {
                connected = false;
                Log.d("BTThread","Failed to send");
            }
        }
    }

    public void writeMessage(String msg) throws InterruptedException{
        BluetoothSocket connSock;

        connSock = bluetoothSocket;

        if(connSock!=null){
            try {

                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connSock.getOutputStream()));
                out.write(msg);
                // out.flush();

                Thread.sleep(30);


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else{
            //Error
        }
    }


}
