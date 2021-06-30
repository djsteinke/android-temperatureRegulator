package rnfive.htfu.temperatureregulator.define;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
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

import static rnfive.htfu.temperatureregulator.define.Constants.convertTemp;

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

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(getView(pos, v.isActivated()));
        builder.setPositiveButton("OK", (dialog, which) -> {
            int time = (pos<2 && !v.isActivated()?context.getResources().getIntArray(R.array.time_int_array)[p1.getValue()]:0);
            String program = (pos==2?p1.getDisplayedValues()[p1.getValue()]:"none");
            double temp = (p2.getValue()>0?convertTemp((double)p2.getValue(), null):0.0);
            String url = "run?type=" + type + "&time=" + time + "&temp=" + temp + "&program=" + program;
            if (v.isActivated())
                url = "cancel?type=" + type;
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
            if (pos == 2)
                p1Values = new String[] {"Main"};
            p1.setMinValue(0);
            p1.setMaxValue(p1Values.length - 1);
            p1.setDisplayedValues(p1Values);
            p1.setWrapSelectorWheel(false);
            p2.setMinValue(75);
            p2.setMaxValue(175);
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
