package com.kryssz.lego3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.StrictMode;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Message;
import android.os.Handler.Callback;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import android.bluetooth.BluetoothAdapter;
import android.widget.Toast;


public class Foablak extends ActionBarActivity{

    public float sensorBear = 0;
    public String BearString = "default";

    BTThread btThread ;
    Thread btThreadT ;

    HTTPThread httpThread;
    Thread httpThreadT;

    double lat = 0;
    double lon = 0;
    float acc = 0;
    double speed=0;
    double bear = 0;
    String other = "";
    Timer t = new Timer();
    TextView txLat;
    TextView txLon;
    TextView txExtra;
    TextView txAcc;
    TextView txBear;
    TextView txSpeed;
    TextView txDLat;
    TextView txDLon;



    Bearsensor bs;
    GPSTracker tracker = new GPSTracker(this);


    private Toast reusableToast;
    private Handler btcHandler;
    private boolean connected = false;
    private ProgressDialog connectingProgressDialog;
    private List<String> programList;
    private TextToSpeech mTts;
    private boolean btErrorPending = false;
    private Activity thisActivity;
    int mRobotType;
    int motorLeft;
    private boolean pairing;



    private boolean allowHTTPCommunication = false;
    private boolean allowBTCommunication = false;


    long lastDataSent=0;
    long lastDataGet=0;

    double destLat = 0;
    double destLon = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       /* StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
*/
        setContentView(R.layout.activity_foablak);
        txLat = (TextView) findViewById(R.id.txLat);
        txLon = (TextView) findViewById(R.id.txLon);
        txAcc = (TextView) findViewById(R.id.txAcc);
        txBear = (TextView) findViewById(R.id.txBear);
        txSpeed = (TextView) findViewById(R.id.txSpeed);
        txExtra = (TextView) findViewById(R.id.txExtra);
        txDLat = (TextView) findViewById(R.id.txDLat);
        txDLon = (TextView) findViewById(R.id.txDLon);



        btThread = new BTThread();
        btThreadT = new Thread(btThread);
       // btThreadT.start();
      //  btThread.run();

        bs = new Bearsensor(this, (SensorManager) getSystemService(SENSOR_SERVICE));
        tracker = new GPSTracker(this);

        t.scheduleAtFixedRate(new locationTask(), 0, 250);



        Button btconn = (Button) findViewById(R.id.btConn);
        btconn.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                btThread.setConnect(true);
            }
        });

        final Button btdebug = (Button) findViewById(R.id.btDebug);
        btdebug.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                try {
                    Log.d("Dest","Szamol");
                    Navigation n = new Navigation();
                    Location s1 = new Location("gps");
                    s1.setLatitude(47.198898);
                    s1.setLongitude(18.399760);
                    n.addLocation(s1);

                    Location s2 = new Location("gps");
                    s2.setLatitude(47.198708);
                    s2.setLongitude(18.400028);
                    n.addLocation(s2);

                    Location s3 = new Location("gps");
                    s3.setLatitude(47.198614);
                    s3.setLongitude(18.400294);
                    n.addLocation(s3);

                    Location s4 = new Location("gps");
                    s4.setLatitude(47.19857);
                    s4.setLongitude(18.400646);
                    n.addLocation(s4);

                    Location dest1 = new Location("gps");
                    dest1.setLatitude(47.199339);
                    dest1.setLongitude(18.400643);
                    n.setDestination(dest1);

                    Log.d("Dest1", String.valueOf(n.getBearingToDest()));

                    dest1 = new Location("gps");
                    dest1.setLatitude(47.198581);
                    dest1.setLongitude(18.401974);
                    n.setDestination(dest1);

                    Log.d("Dest2", String.valueOf(n.getBearingToDest()));

                    Log.d("Heading", String.valueOf(n.getHeading()));

                    btdebug.setText(String.valueOf(n.getHeading()));

                }
                catch(Exception e)
                {
                    Log.d("Dest","Valami bug");
                    e.printStackTrace();
                }

            }
        });



    }

    class locationTask extends TimerTask {

        @Override
        public void run() {

            h.sendEmptyMessage(0);
        }
    };

    public String l0(int szam, int n)
    {
        String s = String.valueOf(szam);
        while(s.length()<n)
        {
            s = "0"+s;
        }
        return s;
    }

    public String getTime()
    {
        Calendar c = Calendar.getInstance();
        String s = "";
        s += l0(c.get(Calendar.HOUR_OF_DAY),2);
        s += ":";
        s += l0(c.get(Calendar.MINUTE),2);
        s += ":";
        s += l0(c.get(Calendar.SECOND), 2);
        return s;
    }

    final Handler h = new Handler(new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            try {
                long time = System.currentTimeMillis();
                HTTPHandler http = new HTTPHandler("http://lego.amk.uni-obuda.hu/legogroup3/php/recieve.php");

                getLocation();

                txLat.setText(DoubleToString(lat,6));
                txLon.setText(DoubleToString(lon,6));
                txSpeed.setText(DoubleToString(speed,6));
                txBear.setText(DoubleToString(sensorBear,6));
                txAcc.setText(String.valueOf(acc));

                if(lat==-2 || other=="null")
                {
                    http.add("hassignal",0);

                    if(allowHTTPCommunication && time-lastDataSent>1000)
                    {
                        http.send();
                        lastDataSent=System.currentTimeMillis();
                    }

                }
                else
                {

                    Calendar c = Calendar.getInstance();
                    //long time = c.getTimeInMillis();
                    http.add("has",1);
                    http.add("lat", lat);
                    http.add("lon", lon);
                    http.add("bear", sensorBear);
                    http.add("acc", acc);
                    http.add("time", time);

                    if(allowHTTPCommunication && time-lastDataSent>1000)
                    {
                        http.send();
                        lastDataSent=System.currentTimeMillis();
                    }
                }
                other += " lchk: "+getTime();
                other = (btThread.getConnected()? "C":"c") + (btThread.isBusy() ? "B" : "b") + (btThread.getConnect() ? "%" : "/");
                txExtra.setText(other);


                HTTPHandler reciever = new HTTPHandler("http://lego.amk.uni-obuda.hu/legogroup3/php/getdestination.php");
                if(allowHTTPCommunication && time-lastDataGet>500) {
                    reciever.send();

                    String[] datas = reciever.response.split(";");

                    destLat = Double.valueOf(datas[0]);
                    destLon = Double.valueOf(datas[1]);

                    txDLat.setText(String.valueOf(destLat));
                    txDLon.setText(String.valueOf(destLon));


                    lastDataGet = System.currentTimeMillis();
                }

            }
            catch (Exception e)
            {
                txLat.setText("Exception");
                txLon.setText(String.valueOf(e.getMessage()));
                e.printStackTrace();

            }

            CheckBox cbBt = (CheckBox) findViewById(R.id.cbBT);
            allowBTCommunication = cbBt.isChecked();

            if(allowBTCommunication) {
               // String msg1 = (sensorBear-90) + ",0";

                int fok = 0;
                int speed = 0;
                if(destLat > 0 && destLat<2 && destLon>0 && destLon<2)
                {
                    if(destLat >= 1)
                    {
                        fok = (int) Math.round((destLat-1)*100);
                    }
                    else
                    {
                        fok = (int) Math.round(-(destLat*100));
                    }


                    speed = (int) Math.round((destLon*1000));
                }
                else
                {
                    fok = speed = 0;
                }


                btThread.addMessage(""+fok+","+speed+"");

            }
            btThreadT = new Thread(btThread);
            btThreadT.run();

            return false;
        }
    });

    private void getLocation() {

       GPSTracker tracker = new GPSTracker(this);

        if (tracker.canGetLocation() == false) {

            lat = -2;
            lon = -2;

        } else {
            lat = tracker.getLatitude();
            lon = tracker.getLongitude();

            if(tracker.getlocation() == null)
            {
                other = "null";
            }
            else
            {
                acc = tracker.getlocation().getAccuracy();
                bear = sensorBear;
                speed = tracker.getlocation().getSpeed();

                Bundle b = tracker.getlocation().getExtras();
                try
                {
                    int stats = b.getInt("satellites");
                    other = String.valueOf("Satellites:"+String.valueOf(stats));
                }
                catch (Exception e)
                {
                    other = String.valueOf("Satellites: unknown");
                }

            }


        }
    }

    public String DoubleToString(double number, int precision)
    {
        NumberFormat nf = new DecimalFormat("#.######");
        return nf.format(number);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_foablak, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private int byteToInt(byte byteValue) {
        int intValue = (byteValue & (byte) 0x7f);

        if ((byteValue & (byte) 0x80) != 0)
            intValue |= 0x80;

        return intValue;
    }

}
