package rnfive.htfu.temperatureregulator.define;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Program {

    private String name;
    private String description;
    private List<Step> steps = new ArrayList<>();

    public Program() {}

    public void addStep(Step s) {
        steps.add(s);
    }
    public void updateStep(Step s) {
        int i = steps.indexOf(s);
        steps.set(i, s);
    }
    public void removeStep(Step s) {
        steps.remove(s);
        resetStepIds();
    }

    private void resetStepIds() {
        for (Program.Step step : steps) {
            step.setId(steps.indexOf(step));
        }
    }

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
    @ToString
    public static class Step {
        @SerializedName("step")
        private int id;
        private int temperature;    // deg C
        private int rate;           // deg C/min
        private int time;           // min
        private boolean vacuum;
        @Expose(serialize = false, deserialize = false)
        private int programPos;

        public Step() {}
        public Step(int step, int temperature, int rate, int time, boolean vacuum, int programPos) {
            this.id = step;
            this.temperature = temperature;
            this.rate = rate;
            this.time = time;
            this.vacuum = vacuum;
            this.programPos = programPos;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + id;
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Step))
                return false;
            Step other = (Step)o;
            return this.id == other.id;
        }
    }
}
