package com.example.temperatureregulator.define;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Program {

    private String name;
    private List<Step> steps;

    public Program() {}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Program))
            return false;
        Program other = (Program)o;
        return this.name.equals(other.name);
    }

    @Getter
    @Setter
    public static class Step {
        private int step;
        private int temperature;    // deg C
        private int rate;           // deg C/min
        private int time;           // min
        private boolean vacuum;

        public Step() {}
    }
}
