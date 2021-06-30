package rnfive.htfu.temperatureregulator.define;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import rnfive.htfu.temperatureregulator.MainActivity;
import rnfive.htfu.temperatureregulator.UrlListener;

public class UrlPostRunnable implements Runnable {
    private static final String TAG = UrlPostRunnable.class.getSimpleName();
    private final UrlListener listener;
    private final String urlBase = "http://" + MainActivity.settings.getIp() + ":" + MainActivity.settings.getPort() + "/";
    private final String endPoint;
    private final String msg;

    public UrlPostRunnable(UrlListener listener, String endPoint, String msg) {
        this.listener = listener;
        this.endPoint = endPoint;
        this.msg = msg;
    }

    @Override
    public void run() {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlBase + endPoint);
            Log.d(TAG, url.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(5000);
            con.setConnectTimeout(5000);
            con.setUseCaches(false);
            con.setAllowUserInteraction(false);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            try(OutputStream os = con.getOutputStream()) {
                byte[] input = msg.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),StandardCharsets.UTF_8));
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
