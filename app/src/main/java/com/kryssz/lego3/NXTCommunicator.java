package com.kryssz.lego3;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Kryssz on 2015.03.14..
 */
public class NXTCommunicator {

    BTConnector connector;
    boolean connected;

    public static byte DIRECT_COMMAND_REPLY = 0x00;
    public static byte SYSTEM_COMMAND_REPLY = 0x01;
    public static byte REPLY_COMMAND = 0x02;
    public static byte DIRECT_COMMAND_NOREPLY = (byte) 0x80; // Avoids ~100ms latency
    public static byte SYSTEM_COMMAND_NOREPLY = (byte) 0x81; // Avoids ~100ms latency

    // Direct Commands
    public static final byte START_PROGRAM = 0x00;
    public static final byte STOP_PROGRAM = 0x01;
    public static final byte PLAY_SOUND_FILE = 0x02;
    public static final byte PLAY_TONE = 0x03;
    public static final byte SET_OUTPUT_STATE = 0x04;
    public static final byte SET_INPUT_MODE = 0x05;
    public static final byte GET_OUTPUT_STATE = 0x06;
    public static final byte GET_INPUT_VALUES = 0x07;
    public static final byte RESET_SCALED_INPUT_VALUE = 0x08;
    public static final byte MESSAGE_WRITE = 0x09;
    public static final byte RESET_MOTOR_POSITION = 0x0A;
    public static final byte GET_BATTERY_LEVEL = 0x0B;
    public static final byte STOP_SOUND_PLAYBACK = 0x0C;
    public static final byte KEEP_ALIVE = 0x0D;
    public static final byte LS_GET_STATUS = 0x0E;
    public static final byte LS_WRITE = 0x0F;
    public static final byte LS_READ = 0x10;
    public static final byte GET_CURRENT_PROGRAM_NAME = 0x11;
    public static final byte MESSAGE_READ = 0x13;

    // NXJ additions
    public static byte NXJ_DISCONNECT = 0x20;
    public static byte NXJ_DEFRAG = 0x21;

    // MINDdroidConnector additions
    public static final byte SAY_TEXT = 0x30;
    public static final byte VIBRATE_PHONE = 0x31;
    public static final byte ACTION_BUTTON = 0x32;

    // System Commands:
    public static final byte OPEN_READ = (byte) 0x80;
    public static final byte OPEN_WRITE = (byte) 0x81;
    public static final byte READ = (byte) 0x82;
    public static final byte WRITE = (byte) 0x83;
    public static final byte CLOSE = (byte) 0x84;
    public static final byte DELETE = (byte) 0x85;
    public static final byte FIND_FIRST = (byte) 0x86;
    public static final byte FIND_NEXT = (byte) 0x87;
    public static final byte GET_FIRMWARE_VERSION = (byte) 0x88;
    public static final byte OPEN_WRITE_LINEAR = (byte) 0x89;
    public static final byte OPEN_READ_LINEAR = (byte) 0x8A;
    public static final byte OPEN_WRITE_DATA = (byte) 0x8B;
    public static final byte OPEN_APPEND_DATA = (byte) 0x8C;
    public static final byte BOOT = (byte) 0x97;
    public static final byte SET_BRICK_NAME = (byte) 0x98;
    public static final byte GET_DEVICE_INFO = (byte) 0x9B;
    public static final byte DELETE_USER_FLASH = (byte) 0xA0;
    public static final byte POLL_LENGTH = (byte) 0xA1;
    public static final byte POLL = (byte) 0xA2;

    public static final byte NXJ_FIND_FIRST = (byte) 0xB6;
    public static final byte NXJ_FIND_NEXT = (byte) 0xB7;
    public static final byte NXJ_PACKET_MODE = (byte) 0xff;

    // Error codes
    public static final byte MAILBOX_EMPTY = (byte) 0x40;
    public static final byte FILE_NOT_FOUND = (byte) 0x86;
    public static final byte INSUFFICIENT_MEMORY = (byte) 0xFB;
    public static final byte DIRECTORY_FULL = (byte) 0xFC;
    public static final byte UNDEFINED_ERROR = (byte) 0x8A;
    public static final byte NOT_IMPLEMENTED = (byte) 0xFD;

    // Firmware codes
    public static byte[] FIRMWARE_VERSION_LEJOSMINDDROID = {0x6c, 0x4d, 0x49, 0x64};


   // BluetoothConnecter btc;

    String mac;

    double voltage = 0;

    boolean requested = false;

    public NXTCommunicator(String mac) {
        this.mac = mac;

    }

    public void connect() {
        connector = new BTConnector(mac);
        connector.setBluetooth(BTConnector.BT_ON);
        Log.d("NXTconn", "Conn started");
        connector.connect();
        Log.d("NXTconn", "Conn progstart");
        connected = true;

        startProgram("navaltest2.rxe");
        Log.d("NXTconn", "Conn end");
    }

    public void disconnect() {
        connector.setBluetooth(BTConnector.BT_OFF);
        connected = false;
    }

    public void getBattery() {

        byte[] t = getGetBatteryLevelMessage();
        connector.sendMessage(t);
        requested = true;

    }

    public void readMessage()
    {
        byte[] b = connector.readMessage();
        if(b.length > 0) {
            if (b[1] == 0x0B) {
                int volt = b[3]  + b[4]* 256;
                voltage = volt / 1000d;
                String dec="";
                for(int i = 0;i<b.length;i++)
                {
                    dec += "["+String.valueOf(b[i])+"],";
                }
                Log.d("Bt",dec);
            }
        }
    }

    public void startProgram(String Progname)
    {
        byte[] t = getStartProgramMessage(Progname);
        try {
            String dec="";
            for (int i=0; i<t.length; i++)
            {
                dec += "["+String.valueOf(t[i])+"],";
            }
          //  Log.d("Startpg",dec);
            connector.sendMessage(t);
        }
        catch (Exception e)
        {
            Log.d("Error",e.getMessage());
            //connected = false;
           /* if(e.getMessage().equals("Broken pipe") || e.getMessage().equals("Transport endpoint is not connected"));
            {
                connect();
            }*/
        }
    }

    public boolean isConnected()
    {
        if(connector != null)
        {
            if(connector.connected == false)
            {
                connected = false;

            }
        }
        return connected;
    }

    public void sendMessage(String message)
    {
        byte[] t;
        message = " "+message;
        t = message.getBytes();

        t= getWriteMessage2(0,t,t.length);

        try {
            String decoded = new String(t, "UTF-8");
           // Log.d("Convert", decoded);
        }
        catch (Exception e)
        {
          //  Log.d("Convert","fail");
        }

        String dec = "";
        for (int i=0; i<t.length; i++)
        {
            dec += "["+String.valueOf(t[i])+"],";
        }

        //Log.d("Array",dec);

        try {
            connector.sendMessage(t);
        }
        catch (Exception e)
        {
            Log.d("BT Error",e.getMessage());
           /* if(e.getMessage().equals("Broken pipe") || e.getMessage().equals("Transport endpoint is not connected"));
            {
                connect();
            }*/
            //connected = false;
        }
    }

    public static byte[] getGetBatteryLevelMessage()
    {
        byte[] message = new byte[2];
        message[0] = DIRECT_COMMAND_REPLY;
        message[1] = GET_BATTERY_LEVEL;

        return message;

    }

    public static byte[] getMotorMessage(int motor, int speed) {
        byte[] message = new byte[12];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = SET_OUTPUT_STATE;
        // Output port
        message[2] = (byte) motor;

        if (speed == 0) {
            message[3] = 0;
            message[4] = 0;
            message[5] = 0;
            message[6] = 0;
            message[7] = 0;

        } else {
            // Power set option (Range: -100 - 100)
            message[3] = (byte) speed;
            // Mode byte (Bit-field): MOTORON + BREAK
            message[4] = 0x03;
            // Regulation mode: REGULATION_MODE_MOTOR_SPEED
            message[5] = 0x01;
            // Turn Ratio (SBYTE; -100 - 100)
            message[6] = 0x00;
            // RunState: MOTOR_RUN_STATE_RUNNING
            message[7] = 0x20;
        }

        // TachoLimit: run forever
        message[8] = 0;
        message[9] = 0;
        message[10] = 0;
        message[11] = 0;

        return message;

    }

    public static byte[] getMotorMessage(int motor, int speed, int end) {
        byte[] message = getMotorMessage(motor, speed);

        // TachoLimit
        message[8] = (byte) end;
        message[9] = (byte) (end >> 8);
        message[10] = (byte) (end >> 16);
        message[11] = (byte) (end >> 24);

        return message;
    }

    public static byte[] getBeepMessage(int frequency, int duration) {
        byte[] message = new byte[6];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = PLAY_TONE;
        // Frequency for the tone, Hz (UWORD); Range: 200-14000 Hz
        message[2] = (byte) frequency;
        message[3] = (byte) (frequency >> 8);
        // Duration of the tone, ms (UWORD)
        message[4] = (byte) duration;
        message[5] = (byte) (duration >> 8);

        return message;
    }

    public static byte[] getWriteMessage(int handle, byte[] data, int dataLength) {
        byte[] message = new byte[dataLength + 3];

        message[0] = SYSTEM_COMMAND_REPLY;
        message[1] = WRITE;

        // copy handle
        message[2] = (byte) handle;
        // copy data
        System.arraycopy(data, 0, message, 3, dataLength);

        return message;
    }

    public static byte[] getWriteMessage2(int handle, byte[] data, int dataLength) {
        byte[] message = new byte[dataLength + 4];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = MESSAGE_WRITE;

        // copy handle
        message[2] = (byte) handle;
        // message[3] = (byte) handle;
        // copy data
        System.arraycopy(data, 0, message, 3, dataLength);

        message[message.length-1]=0x00;

        return message;
    }

    public static byte[] getCloseMessage(int handle) {
        byte[] message = new byte[3];

        message[0] = SYSTEM_COMMAND_REPLY;
        message[1] = CLOSE;

        // copy handle
        message[2] = (byte) handle;

        return message;
    }

    public static byte[] getStartProgramMessage(String programName) {
        byte[] message = new byte[22];

        message[0] = DIRECT_COMMAND_NOREPLY;
        message[1] = START_PROGRAM;

        byte[] prog = programName.getBytes();

        // copy programName and end with 0 delimiter
        for (int pos=0; pos<19; pos++)
        {
            if(pos < prog.length)
            {
                message[2 + pos] = prog[pos];
            }
            else
            {
                message[2+pos] = 0x00;
            }
        }



        //message[programName.length()+2] = 0;

        return message;
    }
}



