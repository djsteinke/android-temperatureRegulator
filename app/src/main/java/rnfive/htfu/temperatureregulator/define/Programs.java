package rnfive.htfu.temperatureregulator.define;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import rnfive.htfu.temperatureregulator.MainActivity;

@Getter
@Setter
public class Programs {
    private static final String TAG = Programs.class.getSimpleName();
    private static final String FILE_NAME = "programs.json";

    private List<Program> programList = new ArrayList<>();

    public Programs() {}

    public List<Program> list() {
        return programList;
    }
    public void addProgram(Program p) {
        programList.add(p);
    }
    public void removeProgram(Program p) {
        programList.remove(p);
    }

    public void save() {
        File f = new File(MainActivity.file, FILE_NAME);
        try {
            if (f.exists() || f.createNewFile()) {
                Constants.save(f, this);
            }
        } catch (IOException e) {
            Log.e(TAG, "save() failed. Error: " + e.getMessage());
        }
    }

    public static Programs fromFile() {
        File f = new File(MainActivity.file, FILE_NAME);
        try {
            return Constants.load(f, Programs.class);
        } catch (IOException e) {
            Log.e(TAG, "fromFile() failed. Error: " + e.getMessage());
            Programs p = new Programs();
            p.save();
            return p;
        }
    }

}
