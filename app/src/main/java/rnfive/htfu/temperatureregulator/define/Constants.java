package rnfive.htfu.temperatureregulator.define;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class Constants {


    public static String formatInt(int val) {
        return String.format(Locale.US,"%d",val);
    }

    public static double convertTemp(Double f, Double c) {
        if (f != null)
            return (f-32.0)/1.8;
        else
            return c*1.8 + 32;
    }

    public static <T> T fromJsonString(String in, Class<T> inClass) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        return gson.fromJson(in, inClass);
    }

    public static <T> String getJsonString(T in) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(in);
    }

    public static <T> void save(File file, T in) throws IOException {
        String val = getJsonString(in);
        if (file.exists() || (!file.exists() && file.createNewFile())) {
            if (file.canWrite() || file.setWritable(true)) {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write(val);
                bw.close();
            }
        }
    }

    public static <T> T load(File file, Class<T> in) throws IOException {
        String inputLine;
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        while ((inputLine = br.readLine()) != null) {
            sb.append(inputLine);
        }
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(sb.toString(), in);
    }

}
