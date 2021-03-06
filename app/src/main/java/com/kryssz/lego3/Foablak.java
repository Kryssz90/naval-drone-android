package com.kryssz.lego3;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.AsyncTask;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    boolean allowMotorMovement = false;

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

    private boolean allowHTTPCommunication = true;
    private boolean allowBTCommunication = true;

    double turndivide = 4;
    double voltageoff = 7;
    double voltageon = 8;
    double turncorrection = 0;
    int minturn = 20;


    long lastDataSent=0;
    long lastStatSent=0;
    long lastDataGet=0;

    int btfok = 0;
    int btspeed = 0;

    //String ki = "";

    HTTPHandler reciever;

    double destLat = 0;
    double destLon = 0;

    int n = 0;

    Navigation nav = new Navigation();

    //BluetoothConnecter btc;
    //final Foablak f = this;

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

        final Button btnGetBattery = (Button) findViewById(R.id.btnGetBattery);
        btnGetBattery.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                btThread.getBattery();

            }
        });


        final Button btSettings = (Button) findViewById(R.id.btnSettings);
        btSettings.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                    //btdebug.setText(String.valueOf(n.getHeading()));
                Intent s = new Intent(getApplicationContext(),Settings.class);
                s.putExtra("turndivide",turndivide);
                s.putExtra("turnonvolt",voltageon);
                s.putExtra("turnoffvolt",voltageoff);
                s.putExtra("turncorrection",turncorrection);
                s.putExtra("miminumturnspeed",minturn);
                startActivityForResult(s,101);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 101){

            // Storing result in a variable called myvar
            // get("website") 'website' is the key value result data
          //  String mywebsite = data.getExtras().get("result");
            turndivide = data.getExtras().getDouble("turndivide");
            voltageon= data.getExtras().getDouble("turnonvolt");
            voltageoff = data.getExtras().getDouble("turnoffvolt");
            turncorrection = data.getExtras().getDouble("turncorrection");
            minturn = data.getExtras().getInt("miminumturnspeed");
        }

    }

    class locationTask extends TimerTask {

        @Override
        public void run() {

            h.sendEmptyMessage(0);
        }
    }

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

    double normalizeAngle(double angle)
    {
        while(angle > 360) angle = angle - 360;
        while(angle < 0) angle = angle + 360;
        return angle;
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

                if(System.currentTimeMillis() - lastStatSent > 1000 ) {
                    HTTPHandler statsend = new HTTPHandler("http://lego.amk.uni-obuda.hu/legogroup3/php/setstatus.php");
                    statsend.add("battery", 100);
                    statsend.add("signal", 0);
                    statsend.add("bluetooth", (btThread.getConnected() ? 1 : 0));
                    statsend.add("gps", ((lat > 1 && lon > 1) ? 1 : 0));
                    statsend.add("voltage", btThread.getVoltage());
                    statsend.send();
                    lastStatSent = System.currentTimeMillis();
                }


                if(lat==-2 || other.equals("null"))
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
                other = (btThread.getConnected()? "C":"c") + (allowMotorMovement ? "M" : "m") + (btThread.getConnect() ? "%" : "/");
                other += "{"+btfok+", "+btspeed+"}";
                other += " "+btThread.getVoltage();
                txExtra.setText(other);

                if(reciever != null)
                {
                    if(reciever.done)
                    {
                        String response =  reciever.response;
                        Log.d("GetDestination", " {"+response+"}");
                        String[] datas = response.split(";");

                        try {
                            destLat = Double.valueOf(datas[0]);
                            destLon = Double.valueOf(datas[1]);
                        }
                        catch(Exception e)
                        {
                            Log.d("GetDestination","Hiba a konvertáláskor, ");
                        }

                        txDLat.setText(String.valueOf(destLat));
                        txDLon.setText(String.valueOf(destLon));

                        reciever = null;

                    }
                }

                if(allowHTTPCommunication)
                {


                    if (time - lastDataGet > 250) {
                        reciever = new HTTPHandler("http://lego.amk.uni-obuda.hu/legogroup3/php/getdestination.php");
                        reciever.send();

                        lastDataGet = System.currentTimeMillis();
                    }
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

            try {

                btfok = 0;
                btspeed = 0;

                // Ha lapátfok és sebességadat érkezik
                if (destLat > 0 && destLat < 2 && destLon > -1 && destLon < 2) {
                    if (destLat >= 1) {
                        btfok = (int) Math.round((destLat - 1) * 100);
                    } else {
                        btfok = (int) Math.round(-(destLat * 100));
                    }
                    btspeed = (int) Math.round((destLon * 1000));
                }
                else if (destLat > 2 && destLat < 3 && destLon > 0 && destLon < 2) {

                    int fokbe = (int) Math.round((destLat - 2) * 1000);

                    double fok1 = (fokbe - sensorBear);

                    while(fok1 < -180) fok1 += 360;
                    while(fok1 > 180) fok1 -= 360;


                    btfok = (int)( Math.round((fok1)/turndivide));

                    if(btfok > 90) btfok = 90;
                    if(btfok < -90) btfok = -90;



                    btspeed = (int) Math.round((destLon * 1000));


                } else if (destLat > 3 && destLon > 2) {
                    nav.setDestination(destLat, destLon);
                    nav.addLocation(lat, lon);

                    double todest = nav.getBearingToDest();

                    double fok1 = (todest - sensorBear);
                   // Log.d("Fok",String.valueOf(fok1));
                    while(fok1 < -180) fok1 += 360;
                    while(fok1 > 180) fok1 -= 360;
                  /*  if(fok1 > 90) fok1 = 90;
                    if(fok1 < -90) fok1 = -90;*/

                    btfok = (int)( Math.round((fok1)/turndivide));

                    if(btfok > 90) btfok = 90;
                    if(btfok < -90) btfok = -90;

                    double distance = nav.getDistance();


                    double speed1 = ((minturn - 120d)/90d)*Math.abs(btfok)+120;
                    if(speed1>127) speed1 = 127;
                    if(speed1<0) speed1=0;

                    if (distance > 15) {

                    } else if (distance > 5) {
                        speed1 = speed1 / 2d;
                    } else {
                        speed1 = 0;
                    }

                    btspeed = (int) Math.round(speed1);

                }
               // ki = "{" + btfok + "," + btspeed + "} ";
               //sendCommand();
            }
            catch(Exception e)
            {
                btspeed = 0;
                btfok= 0;
                //ki="Nem lehet kiszámolni";
            }

            if(!allowMotorMovement)
            {
                btspeed = 0;
            }

            btfok += turncorrection;

            if(btThread.getVoltage() <= voltageoff) allowMotorMovement = false;
            if(btThread.getVoltage() >= voltageon) allowMotorMovement = true;


            if(allowBTCommunication) {
               // String msg1 = (sensorBear-90) + ",0";



               btThread.addMessage(""+btfok+","+btspeed+"");



            }

            //btc=new BluetoothConnecter(f);
            //btc.execute(0);
            btThreadT = new Thread(btThread);
            btThreadT.run();

            return false;
        }
    });

    private void sendCommand()
    {
        if(n==0) {
            try {

                URL obj = new URL("http://lego.amk.uni-obuda.hu/legogroup3/sandbox/php/test_setcom.php?deg=" + btfok + "&speed=" + btspeed);
                if (obj == null) Log.d("Sender", "obj null");
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                if (con == null) Log.d("Sender", "con null");
                con.setRequestMethod("GET");

                con.setRequestProperty("User-Agent", "Mozilla/5.0");

                int responseCode = con.getResponseCode();
                //  System.out.println("\nSending 'GET' request to URL : " + http.url);
                //  System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                //print result
                System.out.println(response.toString());

            } catch (Exception e) {
                Log.d("GetPosTest", e.getMessage());
            }
        }
    }

    private void getLocation2()
    {

        if(n==0) {
            try {

                URL obj = new URL("http://lego.amk.uni-obuda.hu/legogroup3/sandbox/php/test_getpos.php");
                if (obj == null) Log.d("Sender", "obj null");
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                if (con == null) Log.d("Sender", "con null");
                con.setRequestMethod("GET");

                con.setRequestProperty("User-Agent", "Mozilla/5.0");

                int responseCode = con.getResponseCode();
                //  System.out.println("\nSending 'GET' request to URL : " + http.url);
                //  System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                //print result
                System.out.println(response.toString());
                String[] data = response.toString().split(";");


                lat = Double.valueOf(data[0]);
                lon = Double.valueOf(data[1]);
                sensorBear = Float.valueOf(data[2]);

            } catch (Exception e) {
                Log.d("GetPosTest", e.getMessage());
            }
        }
        n++;
        if(n==1) n=0;
    }

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
       // tracker.stopUsingGPS();
        tracker.onDestroy();

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
