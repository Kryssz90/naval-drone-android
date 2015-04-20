package com.kryssz.lego3;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Kryssz on 2015.02.23..
 */
public class HTTPHandler {

    String url;
    List<String> lista = new ArrayList<String>();
    public String response = null;

    private final String USER_AGENT = "Mozilla/5.0";

    public HTTPHandler(String url_)
    {
        url = url_;
    }

    public void add(String key, String value)
    {
        lista.add(key+"="+value);
    }

    public void add(String key, float value)
    {
        add(key,String.valueOf(value));
    }

    public void add(String key, double value)
    {
        add(key,String.valueOf(value));
    }

    public void add(String key, int value)
    {
        add(key,String.valueOf(value));
    }

    public void add(String key, long value)
    {
        add(key,String.valueOf(value));
    }

    public void send() throws MalformedURLException,ProtocolException,IOException
    {

        String fullurl = url;
        fullurl += "?";
        String elso="";
        for(String s : lista)
        {
            fullurl+=elso+s;
            elso="&";
        }

        URL obj = new URL(fullurl);
        Log.d("FullURL",fullurl);
        if(obj== null)Log.d("Sender", "obj null");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        if(con==null) Log.d("Sender", "con null");
        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

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
        this.response = response.toString();
    }


}
