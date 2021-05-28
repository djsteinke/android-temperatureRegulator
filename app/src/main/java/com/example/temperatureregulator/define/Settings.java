package com.example.temperatureregulator.define;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

import static com.example.temperatureregulator.MainActivity.file;
import static com.example.temperatureregulator.define.Constants.load;
import static com.example.temperatureregulator.define.Constants.save;

@Setter
@Getter
public class Settings {
    private static final String TAG = Settings.class.getSimpleName();
    private static final String fileName = "Settings.json";
    private static final File thisFile = new File(file, fileName);
    private String ip;
    private int port;

    public Settings() {}
    public Settings withIp(String ip) {
        this.ip = ip;
        return this;
    }
    public Settings withPort(int port) {
        this.port = port;
        return this;
    }

    public void toFile() {
        try {
            save(thisFile, this);
        } catch (IOException e) {
            Log.d(TAG, "toFile() failed. Error: " + e.getMessage());
        }
    }

    public static Settings fromFile() {
        try {
            File thisFile = new File(file, fileName);
            return load(thisFile, Settings.class);
        } catch (IOException e) {
            Log.d(TAG, "fromFile() failed. Error: " + e.getMessage());
            Settings settings = new Settings().withIp("192.168.0.151").withPort(1983);
            settings.toFile();
            return settings;
        }
    }
}
