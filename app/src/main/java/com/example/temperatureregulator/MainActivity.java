package com.example.temperatureregulator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        final TextView textView = findViewById(R.id.text);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        button.setOnClickListener(v -> {
            try {
                URL url = new URL("http://192.168.0.151:1983/getTemp");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                //con.setReadTimeout(10000);
                //con.setConnectTimeout(10000);
                //con.setUseCaches(false);
                //con.setAllowUserInteraction(false);
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                //con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.connect();
                System.out.println(con.toString());
                System.out.println(con.getRequestMethod());
                System.out.println(con.getResponseCode());
                //con.setRequestMethod("GET");
                //con.setDoOutput(false);
                //System.out.println(con.getResponseMessage());
                BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String output;
                System.out.println("b");
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                System.out.println(sb.toString());
                textView.setText(sb.toString());
                /*
                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = "RESPONSE: ";
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println(response.toString());
                    textView.setText(response.toString());
                }

                 */
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
        /*
        button.setOnClickListener(v -> {
            try {
                URL url = new URL("http://192.168.0.151:1983/getTemp");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                con.setRequestProperty("Accept", "application/json");
                con.setDoOutput(true);
                String jsonInputString = "{}";
                try(OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println(response.toString());
                }
            } catch (Exception e) {
                System.out.println("Error:" + e.getMessage());
            }
        });

         */
    }
}