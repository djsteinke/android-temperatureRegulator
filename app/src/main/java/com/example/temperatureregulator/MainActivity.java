package com.example.temperatureregulator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements UrlListener {

    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnReload = findViewById(R.id.reload);
        Button btnStart = findViewById(R.id.start_stop);
        textView = findViewById(R.id.text);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        btnReload.setOnClickListener(v -> {
            UrlAsync async = new UrlAsync(this);
            async.execute("GET","getTemp");
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

    private double getTempF(double c) {
        return c*1.8 + 32;
    }

    @Override
    public void onGetComplete(String val) {
        String txt = "";
        DecimalFormat df1 = new DecimalFormat("#.#");
        DecimalFormat df0 = new DecimalFormat("#");
        try {
            JSONObject msg = new JSONObject(val);
            JSONObject object = msg.getJSONObject("current");
            txt = "Humidity: " + df0.format((double)object.get("humidity")) +"%\n";
            txt += "Temp \u00B0C/\u00B0F: " + df1.format((double)object.get("temperature")) + " / " +
                    df1.format(getTempF((double)object.get("temperature"))) +"\n";
            txt += "Step: " + object.get("step") + "\n";
            txt += "Step Time: " + object.get("stepTime") + "\n";
            txt += "Elapsed Time: " + object.get("elapsedTime") + "\n";
            // txt += "Temp \u00B0F: " +  +"\n";

        } catch (JSONException e) {
            Log.e("onGetComplete()", "Error: " + e.getMessage());
        }
        if (textView != null) {
            textView.setText(txt);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        UrlAsync async = new UrlAsync(this);
        async.execute("GET","getTemp");
    }
}