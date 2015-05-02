package com.kryssz.lego3;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class Settings extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent i = getIntent();

        double turndivide = i.getDoubleExtra("turndivide",4);
        EditText te = (EditText) findViewById(R.id.etTurnDivide);
        te.setText(String.valueOf(turndivide));

        double turnonvolt= i.getDoubleExtra("turnonvolt",8);
        EditText teturnon = (EditText) findViewById(R.id.etTurnOnVoltage);
        teturnon.setText(String.valueOf(turnonvolt));

        double turnoffvolt = i.getDoubleExtra("turnoffvolt",7);
        EditText teturnoff = (EditText) findViewById(R.id.etTurnOffVoltage);
        teturnoff.setText(String.valueOf(turnoffvolt));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);



        return true;
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();

        EditText te = (EditText) findViewById(R.id.etTurnDivide);
        EditText teturnon = (EditText) findViewById(R.id.etTurnOnVoltage);
        EditText teturnoff = (EditText) findViewById(R.id.etTurnOffVoltage);
        i.putExtra("turndivide", Double.valueOf(te.getText().toString()) );
        i.putExtra("turnonvolt", Double.valueOf(teturnon.getText().toString()) );
        i.putExtra("turnoffvolt", Double.valueOf(teturnoff.getText().toString()) );
        setResult(101,i);

        Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();
        finish();
        return;
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
}
