package com.kryssz.lego3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Kryssz on 2015.04.11..
 */
public class BTThread extends Thread
{
    Timer t = new Timer();

    boolean conn;
    boolean busy;
    List<Bundle> messages = new ArrayList<Bundle>();


    String NXTmac = "00:16:53:0A:85:22";
    NXTCommunicator nxt= new NXTCommunicator(NXTmac);;

    public  BTThread()
    {
        nxt = new NXTCommunicator(NXTmac);
    }

    @Override
    public void start()
    {


       //t.scheduleAtFixedRate(new btTask(), 0, 100);
    }

    @Override
    public void run()
    {
        //h.sendEmptyMessage(0);
        //t.scheduleAtFixedRate(new btTask(), 0, 100);
        Log.d("BTThread","Msgloop");
        if(conn && !getConnected())
        {
            if(!busy)
            {
                busy = true;
                connect();
                busy = false;
            }
        }

        if( messages.size()>0 && getConnected())
        {
            if(!busy)
            {
                long time = System.currentTimeMillis();
                while(messages.size() > 0 && time-messages.get(0).getLong("time")>1000)
                {
                    messages.remove(0);
                }
                if( messages.size()>0) {
                    sendMessage(messages.get(0).getString("message"));
                    messages.remove(0);
                }
            }
        }
    }

    public void StartProgram(String Progname)
    {
        nxt.startProgram(Progname);
    }

    public synchronized void setConnect(boolean value)
    {
        conn=value;
    }

    public synchronized void addMessage(String Message)
    {
        long time = System.currentTimeMillis();
        Bundle b = new Bundle();
        b.putString("message",Message);
        b.putLong("time",time);

        messages.add(b);
    }

    public synchronized boolean getConnect()
    {
        return conn;
    }

    public synchronized boolean isBusy()
    {
        return busy;
    }

    public synchronized boolean getConnected()
    {
        return nxt.isConnected();
    }

    private void connect()
    {
        busy = true;
        Log.d("BTthread","Conn started");
        nxt.connect();
        Log.d("BTthread","Conn end");
        busy = false;
    }

    private void sendMessage(String Message)
    {
        busy = true;
        nxt.sendMessage(Message);
        busy = false;
    }


    class btTask extends TimerTask {

        @Override
        public void run() {


        }
    };

    final Handler h = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            try {
                Log.d("BTThread", "Msgloop");

                if(conn && !getConnected())
                {
                    if(!busy)
                    {
                        connect();
                    }
                }

                if( messages.size()>0 && getConnected())
                {
                    if(!busy)
                    {
                        long time = System.currentTimeMillis();
                        while(messages.size() > 0 && time-messages.get(0).getLong("time")>1000)
                        {
                            messages.remove(0);
                        }
                        sendMessage(messages.get(0).getString("message"));
                        messages.remove(0);
                    }
                }


                return true;
            }
            catch (Exception e)
            {


                return false;
            }
        }


        });

}
