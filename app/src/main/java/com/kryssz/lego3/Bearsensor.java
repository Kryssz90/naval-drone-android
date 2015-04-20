package com.kryssz.lego3;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


/**
 * Created by Kryssz on 2015.02.24..
 */
public class Bearsensor implements SensorEventListener {

    Foablak sender;
    private SensorManager mSensorManager;


    public Bearsensor(Foablak send, SensorManager sm)
    {
        sender = send;
        mSensorManager = sm;
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);


    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float degree = Math.round(event.values[0]);

        String s = "";
        String elso = "";
        for(float f : event.values)
        {
            s+=elso+String.valueOf(f);
            elso=";";
        }

/*
        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

        currentDegree = -degree;*/
        sender.sensorBear = degree;
        sender.BearString = s;

    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


}
