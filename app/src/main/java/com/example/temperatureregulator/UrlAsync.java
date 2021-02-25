package com.example.temperatureregulator;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlAsync extends AsyncTask<String,Void,String > {
    private static final String TAG = UrlAsync.class.getSimpleName();
    UrlListener listener;
    String urlBase = "https://192.168.0.151:1983/";

    public UrlAsync(UrlListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... urls) {
        try {
            URL url = new URL(urlBase + urls[1]);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            //con.setReadTimeout(10000);
            //con.setConnectTimeout(10000);
            //con.setUseCaches(false);
            //con.setAllowUserInteraction(false);
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            if (urls[0].equals("GET"))
                con.connect();
            else if (urls[0].equals("POST")) {
                // TODO "POST" method
                int val = 1;
            } else
                return "Invalid request.";
            System.out.println(con.toString());
            System.out.println(con.getRequestMethod());
            System.out.println(con.getResponseCode());
            //con.setRequestMethod("GET");
            //con.setDoOutput(false);
            //System.out.println(con.getResponseMessage());
            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
            StringBuilder sb = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
            System.out.println(sb.toString());
            return sb.toString();
        } catch (IOException e) {
            return "GET Failed: " + e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String val) {
        listener.onGetComplete(val);
    }
}
