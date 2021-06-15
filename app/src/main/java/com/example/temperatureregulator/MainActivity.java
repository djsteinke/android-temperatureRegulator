package com.example.temperatureregulator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.example.temperatureregulator.define.ButtonAlert;
import com.example.temperatureregulator.define.Response;
import com.example.temperatureregulator.define.Settings;
import com.example.temperatureregulator.define.Status;
import com.example.temperatureregulator.define.UrlRunnable;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.example.temperatureregulator.define.Constants.convertTemp;
import static com.example.temperatureregulator.define.Constants.formatInt;
import static com.example.temperatureregulator.define.Constants.fromJsonString;

public class MainActivity extends AppCompatActivity implements UrlListener {

    public final static String TAG = MainActivity.class.getSimpleName();

    public static AppCompatImageButton btHeat;
    public static AppCompatImageButton btVacuum;
    public static AppCompatImageButton btProgram;
    public static AppCompatImageButton btRefresh;

    public final static int heat_id = R.id.ib_heat;
    public final static int vacuum_id = R.id.ib_vacuum;
    public final static int program_id = R.id.ib_program;
    public final static int refresh_id = R.id.ib_refresh;

    public static Response response;
    public static File file;
    public static Settings settings;
    private final UrlListener urlListener = this;
    private AppCompatImageView historyView;
    private int iHistW;
    private int iHistH;

    private final Handler refreshHandler = new Handler();
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            getStatus();
            refreshHandler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!setFilePath()) {
            Toast.makeText(this, "File Path Creation Failed.", Toast.LENGTH_SHORT).show();
        }
        settings = Settings.fromFile();
        btHeat = findViewById(R.id.ib_heat);
        btProgram = findViewById(R.id.ib_program);
        btVacuum = findViewById(R.id.ib_vacuum);
        btRefresh = findViewById(R.id.ib_refresh);
        historyView = findViewById(R.id.history_image);
        ViewTreeObserver vto = historyView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                historyView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                iHistW  = historyView.getMeasuredWidth();
                iHistH = historyView.getMeasuredHeight();
                //drawHistory();
            }
        });

        btHeat.setActivated(false);
        btHeat.setOnClickListener(onClick(btHeat, this));

        btVacuum.setActivated(false);
        btVacuum.setOnClickListener(onClick(btVacuum, this));

        btProgram.setActivated(false);
        btProgram.setOnClickListener(onClick(btProgram, this));

        btRefresh.setActivated(false);
        btRefresh.setOnClickListener(onClick(btRefresh, this));

        // getStatus();

    }

    private double[] getMM(Status status) {
        double[] ret = new double[] {0,200};
        if (status.getHistoryList() != null) {
            for (Status.History history : status.getHistoryList()) {
                double tF = convertTemp(null, history.getTemp());
                double tSetF = convertTemp(null, history.getSetTemp());
                if (tF > ret[0])
                    ret[0] = tF;
                if (tSetF > ret[0])
                    ret[0] = tSetF;
                if (tF < ret[1])
                    ret[1] = tF;
            }
            ret[0] += 10;
            ret[1] -= 10;
            return ret;
        } else {
            return new double[] {0,0};
        }
    }

    private void drawHistory() {
        Log.d(TAG, "drawHistory() x["+ iHistW + "] + y[" + iHistH + "]");
        Bitmap bitmap = Bitmap.createBitmap(iHistW, iHistH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        if (response.getStatus() != null && response.getStatus().getHistoryList() != null) {
            Status status = response.getStatus();
            double[] mm = getMM(status);
            Log.d(TAG, "MaxMin: " + Arrays.toString(mm));
            int xOff = 85;
            double xMs = (double) (iHistW - xOff) / (double) status.getRecordingTime();
            double yD = (double) iHistH / (mm[0] - mm[1]);

            Paint pTemp = new Paint();
            pTemp.setStyle(Paint.Style.STROKE);
            pTemp.setColor(getColor(R.color.teal_200));
            pTemp.setStrokeWidth(getPxFromDp(1.5f));

            Paint pH = new Paint();
            pH.setStyle(Paint.Style.STROKE);
            pH.setColor(getColor(R.color.gray));
            pH.setStrokeWidth(1);
            pH.setTextSize(getPxFromDp(10f));

            Paint pTxt = new Paint();
            pTxt.setStyle(Paint.Style.FILL);
            pTxt.setColor(getColor(R.color.gray));
            pTxt.setStrokeWidth(1);
            pTxt.setTextSize(getPxFromDp(15f));
            pTxt.setTextAlign(Paint.Align.RIGHT);

            int i = (int) mm[0] - 10;

            while (i > mm[1]) {
                double y = (mm[0] - i) * yD;
                Path p = new Path();
                p.moveTo(xOff, (float) y);
                p.lineTo(iHistW, (float) y);
                canvas.drawPath(p, pH);
                canvas.drawText(formatInt(i) + "\u00B0", xOff - 10, (float) y + getPxFromDp(5f), pTxt);
                i -= 10;
            }

            int xDiv = (status.getRecordingTime()/1800) + 1;
            for (int d = 0; d < xDiv - 1; d++) {
                double x = xOff + (d*1800) * xMs;
                Path p = new Path();
                p.moveTo((float) x, 0);
                p.lineTo((float) x, iHistH);
                canvas.drawPath(p, pH);
            }

            Path pT = new Path();
            Path pV = new Path();
            boolean move = true;
            double vYold = -1;
            pV.moveTo((float) xOff, (float) iHistH);
            for (Status.History h : status.getHistoryList()) {
                double x = xOff + h.getTime() * xMs;
                double y = (mm[0] - convertTemp(null, h.getTemp())) * yD;
                double vY = (h.isVacuum()?0:iHistH);
                if (x >= xOff) {
                    if (move) {
                        pT.moveTo((float) x, (float) y);
                    } else {
                        pT.lineTo((float) x, (float) y);
                    }
                    if (vY != vYold) {
                        pV.lineTo((float) x, (float) vYold);
                        pV.lineTo((float) x, (float) vY);
                        vYold = vY;
                    }
                    move = false;
                }
            }
            if (status.getHistoryList().size() > 1)
                if (vYold == 0)
                    pV.lineTo((float) iHistW, (float) 0);
                pV.lineTo((float) iHistW, (float) iHistH);
                pV.lineTo((float) xOff, (float) iHistH);
                pV.close();
            pTxt.setColor(getColor(R.color.teal_200));
            pTxt.setAlpha(50);
            canvas.drawPath(pV, pTxt);
            canvas.drawPath(pT, pTemp);
        }
        historyView.setImageBitmap(bitmap);
    }

    private View.OnClickListener onClick(AppCompatImageButton b, MainActivity activity) {
        return view -> {
            if (b.getId() == refresh_id) {
                getStatus();
            } else {
                ButtonAlert alert = new ButtonAlert(activity, activity);
                alert.show(b);
            }
        };
    }

    private float getPxFromDp(Float dip) {
        Resources r = getResources();
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
    }

    private void getStatus() {
        btRefresh.setActivated(true);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new UrlRunnable(this, "get/status"));
    }

    private boolean setFilePath() {
        file = this.getExternalFilesDir("Settings");
        boolean result = file.exists() || file.mkdir();
        return result && (file.canWrite() || file.setWritable(true, true));
    }

    private void refresh(boolean start) {
        if (start)
            refreshHandler.postDelayed(refreshRunnable, 10000);
        else
            refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    public void onGetComplete(String val, String endPoint) {
        String reqType =  endPoint.split("\\?")[0];
        //DecimalFormat df1 = new DecimalFormat("#.#");
        DecimalFormat df0 = new DecimalFormat("#");

        response = fromJsonString(val, Response.class);
        if (response.getCode() == 400) {
            Toast.makeText(this, "Error: " + response.getValue(), Toast.LENGTH_SHORT).show();
            if (reqType.equals("run")) {
                String[] keys =  endPoint.split("\\?")[1].split("&");
                setRunButtonActivated(keys);
            } else {
                reqType =  endPoint.split("/")[1];
                if (reqType.equals("status"))
                    btRefresh.setActivated(false);
            }
            return;
        }
        if (response.getCode() == 200) {
            if (reqType.equals("run") || reqType.equals("cancel")) {
                String[] keys =  endPoint.split("\\?")[1].split("&");
                setRunButtonActivated(keys);
            } else {
                if (response.getType().equals("status") && response.getStatus() != null) {
                    String txt = "Humidity: " + df0.format(response.getStatus().getHumidity()) + "%\n";
                    txt += "Temperature: " + getTempString(response.getStatus().getTemperature()) + "\n";
                    txt += "Step: " + response.getStatus().getStep() + "\n";
                    txt += "Step Time: " + response.getStatus().getStepTime() + "\n";
                    txt += "Hole Temp: " + getTempString(response.getStatus().getHoldTemperature()) + "\n";
                    txt += "Elapsed Step Time: " + response.getStatus().getElapsedStepTime() + "\n";
                    txt += "Heat On: " + (response.getStatus().isHeatOn() ? "ON" : "OFF") + "\n";
                    txt += "Heat Running: " + (response.getStatus().isHeatRunning() ? "ON" : "OFF") + "\n";
                    txt += "Vacuum Running: " + (response.getStatus().isVacuumRunning() ? "ON" : "OFF") +
                            (response.getStatus().isVacuumRunning() ? " [" + response.getStatus().getVacuumTimeRemaining() + "]" : "") + "\n";

                    btHeat.setActivated(response.getStatus().isHeatRunning());
                    btVacuum.setActivated(response.getStatus().isVacuumRunning());
                    btProgram.setActivated(response.getStatus().isProgramRunning());
                    drawHistory();

                    Log.d(TAG, txt);
                    btRefresh.setActivated(false);
                    refresh(true);
                }
                //refreshHandler.postDelayed(refreshRunnable, 5000);
            }
        }
    }

    private void setRunButtonActivated(String[] keys) {
        String type = "";
        int time = 0;
        double temp = 0d;
        String program = "";
        for (String key : keys) {
            String[] vals = key.split("=");
            switch (vals[0]) {
                case "type":
                    type = vals[1];
                    break;
                case "time":
                    time = Integer.parseInt(vals[1]);
                    break;
                case "temp":
                    temp = Double.parseDouble(vals[1]);
                    break;
                case "program":
                    program = vals[1];
                    break;
            }
        }
        switch (type) {
            case "heat":
                btHeat.setActivated(!btHeat.isActivated());
                break;
            case "vacuum":
                btVacuum.setActivated(!btVacuum.isActivated());
                break;
            case "program":
                btProgram.setActivated(!btProgram.isActivated());
                break;
        }
    }

    private String getTempString(double c) {
        DecimalFormat df1 = new DecimalFormat("#.#");
        return df1.format(c) + "\u00B0C [" + df1.format(convertTemp(null, c)) + "\u00B0F]";
    }

    @Override
    public void onResume() {
        super.onResume();
        getStatus();
        //refresh(swcRefresh.isChecked());
    }

    @Override
    public void onPause() {
        super.onPause();
        refresh(false);
    }
}