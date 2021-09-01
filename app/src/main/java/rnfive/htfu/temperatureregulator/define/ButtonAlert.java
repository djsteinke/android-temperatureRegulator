package rnfive.htfu.temperatureregulator.define;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import rnfive.htfu.temperatureregulator.UrlListener;
import rnfive.htfu.temperatureregulator.R;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.appcompat.widget.AppCompatImageButton;
import lombok.Getter;
import lombok.Setter;
import rnfive.htfu.temperatureregulator.MainActivity;

import static java.util.Objects.isNull;
import static rnfive.htfu.temperatureregulator.MainActivity.programs;
import static rnfive.htfu.temperatureregulator.define.Constants.convertTemp;
import static rnfive.htfu.temperatureregulator.define.Constants.tempMaxC;
import static rnfive.htfu.temperatureregulator.define.Constants.tempMinC;

@Getter
@Setter
public class ButtonAlert {
    private final static String TAG = ButtonAlert.class.getSimpleName();
    private final UrlListener listener;
    private final Context context;
    private int pos;
    private final int[] layoutIds = new int[] {R.layout.alert_heat, 0, 0};
    private NumberPicker p1;
    private NumberPicker p2;
    private static final int tempSize = tempMaxC - tempMinC + 1;
    private static final String[] temps = new String[tempSize];
    static {
        for (int c=tempMinC; c<=tempMaxC; c++) {
            int i = c-tempMinC;
            int f = (int) Math.round(convertTemp(null, (double) c));
            temps[i] = c + "\u00B0C [" + f + "\u00B0F]";
        }
    }

    public ButtonAlert(Context context, UrlListener listener) {
        this.listener = listener;
        this.context = context;
    }

    public void show(View v) {
        int pos;
        final String type;
        switch (v.getId()) {
            case MainActivity.program_id:
                pos = 2;
                type = "program";
                break;
            case MainActivity.vacuum_id:
                pos = 1;
                type = "vacuum";
                break;
            default:
                pos = 0;
                type = "heat";
        }
        Log.d(TAG, "pos[" + pos + "]");

        if (pos == 2 && (isNull(programs) || isNull(programs.list()) || programs.list().size() == 0)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(getView(pos, v.isActivated()));
        builder.setPositiveButton("OK", (dialog, which) -> {
            String url;
            if (v.isActivated())
                url = "cancel?type=" + type;
            else {
                int time = (pos < 2 && !v.isActivated() ? context.getResources().getIntArray(R.array.time_int_array)[p1.getValue()] : 0);
                String program = (pos == 2 ? p1.getDisplayedValues()[p1.getValue()] : "none");
                double temp = 0.0d;
                if (p2.getVisibility() != View.GONE)
                    temp = (double) p2.getValue() + tempMinC;
                url = "run?type=" + type + "&time=" + time + "&temp=" + temp + "&program=" + program;
            }
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(new UrlRunnable(listener, url));
        });
        builder.setNegativeButton("CANCEL", (dialog, which) -> {

        });
        builder.create();
        builder.show();
    }

    private View getView(int pos, boolean activated) {
        String[] titles = context.getResources().getStringArray(R.array.alert_titles);
        if (!activated) {
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(context).inflate(R.layout.alert_heat, null);

            p1 = view.findViewById(R.id.picker_1);
            p2 = view.findViewById(R.id.picker_2);
            final TextView title = view.findViewById(R.id.alert_title);
            final TextView t2 = view.findViewById(R.id.title_2);
            if (pos > 0) {
                t2.setVisibility(View.GONE);
                p2.setVisibility(View.GONE);
            }
            String titleValue = context.getString(R.string.start) + " " + titles[pos] + "?";
            title.setText(titleValue);
            String[] p1Values = context.getResources().getStringArray(R.array.time_array);
            if (pos == 2) {
                p1Values = new String[programs.list().size()];
                int i = 0;
                for (Program p : programs.list())
                    p1Values[i++] = p.getName();
            }
            p1.setMinValue(0);
            p1.setMaxValue(p1Values.length - 1);
            p1.setDisplayedValues(p1Values);
            p1.setWrapSelectorWheel(false);
            p2.setMinValue(0);
            p2.setMaxValue(tempSize - 1);
            p2.setDisplayedValues(temps);
            p2.setWrapSelectorWheel(false);
            return view;
        } else {
            @SuppressLint("InflateParams")
            View view = LayoutInflater.from(context).inflate(R.layout.alert_heat, null);

            p1 = view.findViewById(R.id.picker_1);
            p2 = view.findViewById(R.id.picker_2);
            final TextView title = view.findViewById(R.id.alert_title);
            final TextView t2 = view.findViewById(R.id.title_2);
            final TextView t1 = view.findViewById(R.id.title_1);
            t2.setVisibility(View.GONE);
            p2.setVisibility(View.GONE);
            t1.setVisibility(View.GONE);
            p1.setVisibility(View.GONE);
            String titleValue = context.getString(R.string.stop) + " " + titles[pos] + "?";
            title.setText(titleValue);
            return view;
        }
    }
}
