package rnfive.htfu.temperatureregulator.define;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import rnfive.htfu.temperatureregulator.UrlListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import rnfive.htfu.temperatureregulator.MainActivity;

public class UrlRunnable implements Runnable {
    private static final String TAG = UrlRunnable.class.getSimpleName();
    private final UrlListener listener;
    private final String urlBase = "http://" + MainActivity.settings.getIp() + ":" + MainActivity.settings.getPort() + "/";
    private final String endPoint;

    public UrlRunnable(UrlListener listener, String endPoint) {
        this.listener = listener;
        this.endPoint = endPoint;
    }

    @Override
    public void run() {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlBase + endPoint);
            Log.d(TAG, url.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(15000);
            con.setConnectTimeout(15000);
            con.setUseCaches(false);
            con.setAllowUserInteraction(false);
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
            String output;
            while ((output = br.readLine()) != null) {
                sb.append(output);
            }
        } catch (IOException e) {
            Log.e(TAG, "run() error: " + e.getMessage());
            sb.append("{\"code\": 400, \"error\": \"");
            sb.append(endPoint);
            sb.append("\", \"status\": {}, \"type\": null, \"value\": ");
            sb.append("\"");
            sb.append(e.getMessage());
            sb.append("\"}");
        }
        Log.d(TAG, "Response: " + sb.toString());
        final String ret = sb.toString();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> listener.onGetComplete(ret, endPoint));
    }
}
