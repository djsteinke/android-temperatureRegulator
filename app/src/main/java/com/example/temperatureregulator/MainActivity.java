package com.example.temperatureregulator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements UrlListener {

    private TextView textView;
    private Switch swcRefresh;
    private Switch swcHeat;
    private Switch swcVacuum;

    private final UrlListener urlListener = this;

    Handler refreshHandler = new Handler();
    Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            UrlAsync async = new UrlAsync(urlListener);
            async.execute("GET","");
            refreshHandler.postDelayed(refreshRunnable,5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnReload = findViewById(R.id.reload);
        swcRefresh = findViewById(R.id.switch_refresh);
        swcHeat = findViewById(R.id.switch_heat);
        swcVacuum = findViewById(R.id.switch_vacuum);
        textView = findViewById(R.id.text);
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);
        btnReload.setOnClickListener(v -> {
            UrlAsync async = new UrlAsync(this);
            async.execute("GET","getTemp");
        });

        swcRefresh.setOnClickListener(v -> {
            String val = (swcRefresh.isChecked()?"start":"stop");
            refresh(swcRefresh.isChecked());
            Log.d("swcRefresh.setOnClickListener",val);
        });

        swcHeat.setOnClickListener(v -> {
            String val = (swcHeat.isChecked()?"start":"stop");
            UrlAsync async = new UrlAsync(this);
            async.execute("GET","program/"+val);
            Log.d("swcHeat.setOnClickListener",val);
        });

        swcVacuum.setOnClickListener(v -> {
            String val = (swcVacuum.isChecked()?"start":"stop");
            UrlAsync async = new UrlAsync(this);
            async.execute("GET","vacuum/"+val);
            Log.d("swcVacuum.setOnClickListener",val);
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

    private void refresh(boolean start) {
        if (start)
            refreshHandler.post(refreshRunnable);
        else
            refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    public void onGetComplete(String val) {
        String txt = "";
        DecimalFormat df1 = new DecimalFormat("#.#");
        DecimalFormat df0 = new DecimalFormat("#");
        try {
            JSONObject msg = new JSONObject(val);
            if (msg.has("current")) {
                JSONObject object = msg.getJSONObject("current");
                txt = "Humidity: " + df0.format((double) object.getDouble("humidity")) + "%\n";
                txt += "Temperature: " + getTempString((double) object.getDouble("temperature")) + "\n";
                txt += "Step: " + object.getInt("step") + "\n";
                txt += "Step Time: " + object.getInt("stepTime") + "\n";
                txt += "Step Temp: " + getTempString((double) object.getDouble("stepTemperature")) + "\n";
                txt += "Elapsed Time: " + object.getInt("elapsedTime") + "\n";
                txt += "Heat: " + (object.getBoolean("heat")?"ON":"OFF") + "\n";
                txt += "Vacuum: " + (object.getBoolean("vacuum")?"ON":"OFF") +
                        (object.getBoolean("vacuum")?" [" + object.getInt("vacuumTimeRemaining") + "]":"") + "\n";
                swcHeat.setChecked(object.getBoolean("heat"));
                swcVacuum.setChecked(object.getBoolean("vacuum"));
            } else if (msg.has("statusCode")) {
                int code = msg.getInt("statusCode");
                if (code == 200)
                    return;
            }
            // txt += "Temp \u00B0F: " +  +"\n";

        } catch (JSONException e) {
            Log.e("onGetComplete()", "Error: " + e.getMessage());
        }
        if (textView != null) {
            textView.setText(txt);
        }
    }

    private String getTempString(double c) {
        DecimalFormat df1 = new DecimalFormat("#.#");
        return df1.format(c) + "\u00B0C [" + df1.format(getTempF(c)) + "\u00B0F]";
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh(swcRefresh.isChecked());
    }

    @Override
    public void onPause() {
        super.onPause();
        refresh(false);
    }
}