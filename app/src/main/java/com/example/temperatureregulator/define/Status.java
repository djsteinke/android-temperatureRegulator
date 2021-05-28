package com.example.temperatureregulator.define;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Status {
    private double humidity;
    private double temperature;
    private int step;
    private int stepTime;
    private double holdTemperature;
    private int recordingTime;
    private int elapsedStepTime;
    private int elapsedProgramTime;
    private int vacuumTimeRemaining;
    private boolean heatOn;
    private boolean heatRunning;
    private boolean vacuumRunning;
    private boolean programRunning;

    @SerializedName("history")
    private List<History> historyList;

    public Status() {}

    @Getter
    @Setter
    @ToString
    public static class History {
        private int time;
        private double temp;
        private double setTemp;
        private boolean vacuum;

        public History() {}
    }

}
