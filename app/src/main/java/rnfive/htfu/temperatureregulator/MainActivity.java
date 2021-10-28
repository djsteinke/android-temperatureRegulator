package rnfive.htfu.temperatureregulator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import rnfive.htfu.temperatureregulator.adapter.ProgramAdapter;
import rnfive.htfu.temperatureregulator.define.DeleteAlert;
import rnfive.htfu.temperatureregulator.define.OnItemClickListener;
import rnfive.htfu.temperatureregulator.define.Program;
import rnfive.htfu.temperatureregulator.define.Settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import rnfive.htfu.temperatureregulator.define.ButtonAlert;
import rnfive.htfu.temperatureregulator.define.Programs;
import rnfive.htfu.temperatureregulator.define.Response;
import rnfive.htfu.temperatureregulator.define.Status;
import rnfive.htfu.temperatureregulator.define.enums.Action;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;
import static rnfive.htfu.temperatureregulator.define.Constants.df0;
import static rnfive.htfu.temperatureregulator.define.Constants.getTempString;
import static rnfive.htfu.temperatureregulator.define.Constants.tempMaxC;
import static rnfive.htfu.temperatureregulator.define.Menu.menuItemSelector;
import static rnfive.htfu.temperatureregulator.define.enums.Action.*;
import static rnfive.htfu.temperatureregulator.define.Constants.convertTemp;
import static rnfive.htfu.temperatureregulator.define.Constants.formatInt;
import static rnfive.htfu.temperatureregulator.define.Constants.fromJsonString;

public class MainActivity extends AppCompatActivity implements UrlListener, OnItemClickListener {

    public final static String TAG = MainActivity.class.getSimpleName();

    private static final double tempMinDisplay = 15.56;
    private static int tempStartDisplay;

    public static AppCompatImageButton btHeat;
    public static AppCompatImageButton btVacuum;
    public static AppCompatImageButton btProgram;
    public static AppCompatImageButton btRefresh;
    public static AppCompatImageButton btNext;

    public final static int heat_id = R.id.ib_heat;
    public final static int vacuum_id = R.id.ib_vacuum;
    public final static int program_id = R.id.ib_program;
    public final static int refresh_id = R.id.ib_refresh;
    public final static int next_id = R.id.ib_programs;
    public static final int fab_id = R.id.fab;

    public static Programs programs;
    public static Response response;
    public static File file;
    public static Settings settings;
    private ServiceBroadcastReceiver serviceBroadcastReceiver;
    private AppCompatImageView historyView;
    private AppCompatImageView heatLight;
    private ProgramAdapter programAdapter;
    private int iHistW;
    private int iHistH;

    private static boolean receiverRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        load();
        btHeat = findViewById(R.id.ib_heat);
        btProgram = findViewById(R.id.ib_program);
        btVacuum = findViewById(R.id.ib_vacuum);
        btRefresh = findViewById(R.id.ib_refresh);
        historyView = findViewById(R.id.history_image);
        btNext = findViewById(R.id.ib_programs);
        heatLight = findViewById(R.id.heat_light);
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

        btNext.setActivated(false);
        btNext.setOnClickListener(onClick(btNext, this));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(onClick(fab, this));

        serviceBroadcastReceiver = new ServiceBroadcastReceiver(this);

        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setHasFixedSize(true);
        programAdapter = new ProgramAdapter(programs.list(), this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(programAdapter);

    }

    private double[] getMM(Status status) {
        double tMn = convertTemp(null, tempMinDisplay);
        double tMx = convertTemp(null, (double) tempMaxC);
        int tTemp = (int) (tMx/10);
        tMx = (tTemp+1)*10d;
        double[] ret = new double[] {tMx,tMn};
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
            tTemp = (int) (ret[1]/10);
            ret[1] = tTemp*10d;
        }
        return ret;
    }

    private double[] getRMM(Status status) {
        Integer[] ret = new Integer[] {null,null};
        if (status.getHistoryList() != null) {
            for (Status.History history : status.getHistoryList()) {
                int time = history.getTime();
                if (ret[0] == null || time > ret[0])
                    ret[0] = time;
                if (ret[1] == null || time < ret[1])
                    ret[1] = time;
            }
        }
        return new double[] {(double) (ret[0] != null ? ret[0] : 0), (double) (ret[1] != null ? ret[1] : 0)};
    }

    private void drawHistory() {
        Log.d(TAG, "drawHistory() x["+ iHistW + "] + y[" + iHistH + "]");
        Bitmap bitmap = Bitmap.createBitmap(iHistW, iHistH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int iGraphH = iHistH - (int) getPxFromDp(50.0f);
        int iIsOnH = (int) getPxFromDp(20.0f);
        int iIsOnGap = (int) getPxFromDp(5.0f);
        int iVacOnH = iHistH - iIsOnH;
        int iHeatOnH = iVacOnH - iIsOnGap - iIsOnH;
        int iHeatOffH = iHeatOnH + iIsOnH;

        if (response.getStatus() != null && response.getStatus().getHistoryList() != null) {
            Status status = response.getStatus();
            double[] mm = getMM(status);
            double[] rmm = getRMM(status);
            Log.d(TAG, "MaxMin: " + Arrays.toString(mm));
            Log.d(TAG, "MaxMin: " + Arrays.toString(rmm));
            int xOff = 90;
            double xDiff = rmm[0] - rmm[1];
            if (xDiff < 3600) {
                rmm[1] = rmm[0] - 3600;
                xDiff = 3600;
            }
            double xMs = (double) (iHistW - xOff) / xDiff;
            double yD = (double) iGraphH / (mm[0] - mm[1]);

            Paint pTemp = new Paint();
            pTemp.setStyle(Paint.Style.STROKE);
            pTemp.setColor(getColor(R.color.teal_200));
            pTemp.setStrokeWidth(getPxFromDp(1.5f));

            Paint pHeat = new Paint();
            pHeat.setStyle(Paint.Style.STROKE);
            pHeat.setColor(getColor(R.color.red));
            pHeat.setStrokeWidth(getPxFromDp(1.5f));

            Paint pVac = new Paint();
            pVac.setStyle(Paint.Style.STROKE);
            pVac.setColor(getColor(R.color.teal_200));
            pVac.setStrokeWidth(getPxFromDp(1.5f));

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

            int i = (int) mm[0] - 20;

            // Horizontal lines
            while (i > mm[1]) {
                double y = (mm[0] - i) * yD;
                Path p = new Path();
                p.moveTo(xOff, (float) y);
                p.lineTo(iHistW, (float) y);
                canvas.drawPath(p, pH);
                canvas.drawText(formatInt(i) + "\u00B0", xOff - 10, (float) y + getPxFromDp(5f), pTxt);
                i -= 20;
            }

            // Vertical lines ever 15 mins
            int xDiv = (int) (xDiff/900.0d) + 1;
            for (int d = 0; d < xDiv - 1; d++) {
                double x = xOff + (d*900) * xMs;
                Path p = new Path();
                p.moveTo((float) x, 0);
                p.lineTo((float) x, iGraphH);
                canvas.drawPath(p, pH);
            }

            Path pT = new Path();
            Path pVacOn = new Path();
            Path pHeatOn = new Path();
            Path pSetTemp = new Path();
            boolean moveT = true;
            boolean moveST = true;
            float vYold = -1;
            boolean vacuumOn = false;
            boolean heatOn = false;
            for (Status.History h : status.getHistoryList()) {
                float x = (float) (xOff + (h.getTime()-rmm[1]) * xMs);
                float y = (float) ((mm[0] - convertTemp(null, h.getTemp())) * yD);
                float yST = (float) ((mm[0] - convertTemp(null, h.getSetTemp())) * yD);

                if (x >= xOff) {
                    if (moveT) {
                        pT.moveTo(x, (float) iGraphH);
                    } else {
                        pT.lineTo(x, y);
                    }
                    if (moveST && h.getSetTemp() > 0) {
                        pSetTemp.moveTo(x, iGraphH);
                        moveST = false;
                    }
                    if (h.getSetTemp() > 0)
                        pSetTemp.lineTo(x, yST);
                    moveT = false;
                    if (h.isVacuum() && !vacuumOn) {
                        pVacOn.moveTo(x, (float) iHistH);
                        pVacOn.lineTo(x, (float) iVacOnH);
                    } else if (!h.isVacuum() && vacuumOn) {
                        pVacOn.lineTo(x, (float) iVacOnH);
                        pVacOn.lineTo(x, (float) iHistH);
                        pVacOn.close();
                    }
                    vacuumOn = h.isVacuum();

                    if (h.isHeat() && !heatOn) {
                        pHeatOn.moveTo(x, (float) iHeatOffH);
                        pHeatOn.lineTo(x, (float) iHeatOnH);
                    } else if (!h.isHeat() && heatOn) {
                        pHeatOn.lineTo(x, (float) iHeatOnH);
                        pHeatOn.lineTo(x, (float) iHeatOffH);
                        pHeatOn.close();
                    }
                    heatOn = h.isHeat();
                }
            }
            if (vacuumOn) {
                pVacOn.lineTo((float) iHistW, (float) iVacOnH);
                pVacOn.lineTo((float) iHistW, (float) iHistH);
                pVacOn.close();

                pVac.setStyle(Paint.Style.FILL_AND_STROKE);
            }
            canvas.drawCircle((float) (xOff/2), (float) (iVacOnH + iIsOnH/2), (float) iIsOnH/2.0f-getPxFromDp(1.5f), pVac);

            if (heatOn) {
                pHeatOn.lineTo((float) iHistW, (float) iHeatOnH);
                pHeatOn.lineTo((float) iHistW, (float) iHeatOffH);
                pHeatOn.close();

                pHeat.setStyle(Paint.Style.FILL_AND_STROKE);
            }
            canvas.drawCircle((float) (xOff/2), (float) (iHeatOnH + iIsOnH/2), (float) iIsOnH/2.0f-getPxFromDp(1.5f), pHeat);

            pVac.setStyle(Paint.Style.FILL);
            pVac.setAlpha(50);
            canvas.drawPath(pVacOn, pVac);
            pHeat.setStyle(Paint.Style.FILL);
            pHeat.setAlpha(100);
            canvas.drawPath(pHeatOn, pHeat);

            canvas.drawPath(pT, pTemp);
            pTemp.setColor(getColor(R.color.red_dark));
            pTemp.setStrokeWidth(getPxFromDp(1.0f));
            canvas.drawPath(pSetTemp, pTemp);
        }
        historyView.setImageBitmap(bitmap);
    }

    private View.OnClickListener onClick(View v, MainActivity activity) {
        return view -> clickSwitch(v, activity);
    }

    private void clickSwitch(View v,  @Nullable MainActivity activity) {
        switch (v.getId()) {
            case refresh_id:
                break;
            case fab_id:
                programAlert(programs.list().size());
                break;
            case next_id:
                loadProgram(null);
                Intent intent = new Intent(this, ProgramsActivity.class);
                startActivity(intent);
                break;
            default:
                ButtonAlert alert = new ButtonAlert(activity, activity);
                alert.show(v);
        }
    }

    private void loadProgram(@Nullable String name) {
        Intent intent = new Intent(this, ProgramsActivity.class);
        Log.d(TAG, "loadProgram(" + name + ")");
        if (name != null) {
            Bundle bundle = new Bundle();
            bundle.putString(ProgramsActivity.INTENT_NAME, name);
            intent.putExtra(ProgramsActivity.INTENT_BUNDLE, bundle);
        }
        startActivity(intent);
    }

    private float getPxFromDp(Float dip) {
        Resources r = getResources();
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
    }

    private void load() {
        file = this.getExternalFilesDir("Settings");
        boolean result = file.exists() || file.mkdir();
        if (result && (file.canWrite() || file.setWritable(true, true))) {
            programs = Programs.fromFile();
            settings = Settings.fromFile();
        }
    }

    private void service(String serviceAction) {
        Intent serviceIntent = new Intent(this, StatusService.class);
        serviceIntent.setAction(serviceAction);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    @Override
    public void onGetComplete(String val, String endPoint) {
        if (endPoint == null)
            Toast.makeText(this, val, Toast.LENGTH_SHORT).show();
        else {
            String[] epSplit = endPoint.split("\\?");
            String reqType = "";
            if (epSplit.length > 0)
                reqType = epSplit[0];

            response = fromJsonString(val, Response.class);
            if (response.getCode() == 400) {
                String fail = "failed to connect to";
                if (!response.getValue().toLowerCase(Locale.US).contains(fail)) {
                    Toast.makeText(this, "Error: " + response.getValue(), Toast.LENGTH_SHORT).show();
                } else
                    service(StatusService.REFRESH_STATUS);
                if (reqType.equals("run")) {
                    if (epSplit.length > 1) {
                        String[] keys = epSplit[1].split("&");
                        setRunButtonActivated(keys);
                    }
                } else {
                    epSplit = endPoint.split("/");
                    if (epSplit.length > 1)
                        reqType = endPoint.split("/")[1];
                    if (reqType.equals("status"))
                        clickSwitch(btRefresh, null);
                }
                return;
            }
            if (response.getCode() == 200) {
                if (reqType.equals("run") || reqType.equals("cancel")) {
                    String[] keys = endPoint.split("\\?")[1].split("&");
                    setRunButtonActivated(keys);
                } else {
                    if (response.getType().equals("status") && response.getStatus() != null) {
                        service(StatusService.REFRESH_STATUS);
                        String t = getTempString(response.getStatus().getTemperature()) + " @ " +
                                df0.format(response.getStatus().getHumidity()) + "%\n";
                        if (response.getStatus().getStep() >= 0)
                            t += (response.getStatus().getStep() + 1) + " @ " + response.getStatus().getElapsedStepTime() + "["
                                    + response.getStatus().getStepTime() + "]\n";
                        if (response.getStatus().getHoldTemperature() > 0)
                            t += "Hold: " + getTempString(response.getStatus().getHoldTemperature()) + "\n";
                        if (response.getStatus().getRunning() != null && !isEmpty(response.getStatus().getRunning()))
                            t += "Running [" + response.getStatus().getRunning() + "]\n";
                        //t += "Heat [" + (response.getStatus().isHeatRunning() ? "RUNNING | " : "") + (response.getStatus().isHeatOn() ? "ON" : "OFF") + "]\n";
                        //t += "Vacuum [" + (response.getStatus().isVacuumRunning() ? "ON" : "OFF") +
                        //        (response.getStatus().isVacuumRunning() ? " (" + response.getStatus().getVacuumTimeRemaining() + ")" : "") + "]";
                        /*
                        String txt = "Humidity: " + df0.format(response.getStatus().getHumidity()) + "%\n";
                        txt += "Temperature: " + getTempString(response.getStatus().getTemperature()) + "\n";
                        txt += "Step: " + response.getStatus().getStep() + "\n";
                        txt += "Step Time: " + response.getStatus().getStepTime() + "\n";
                        txt += "Hold Temp: " + getTempString(response.getStatus().getHoldTemperature()) + "\n";
                        txt += "Elapsed Step Time: " + response.getStatus().getElapsedStepTime() + "\n";
                        txt += "Heat On: " + (response.getStatus().isHeatOn() ? "ON" : "OFF") + "\n";
                        txt += "Heat Running: " + (response.getStatus().isHeatRunning() ? "ON" : "OFF") + "\n";
                        txt += "Vacuum Running: " + (response.getStatus().isVacuumRunning() ? "ON" : "OFF") +
                                (response.getStatus().isVacuumRunning() ? " [" + response.getStatus().getVacuumTimeRemaining() + "]" : "") + "\n";
                         */
                        TextView tv = findViewById(R.id.status);
                        tv.setText(t);

                        //heatLight.setVisibility((response.getStatus().isHeatOn() ? View.VISIBLE : View.GONE));
                        btHeat.setActivated(response.getStatus().isHeatRunning());
                        btVacuum.setActivated(response.getStatus().isVacuumRunning());
                        btProgram.setActivated(response.getStatus().isProgramRunning());
                        drawHistory();

                        Log.d(TAG, t);
                    }
                }
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

    private void receiver(Action action) {
        if (action == REGISTER) {
            if (!receiverRunning) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(ServiceBroadcastReceiver.ACTION_STATUS);
                registerReceiver(serviceBroadcastReceiver, filter);
            }
            receiverRunning = true;
        } else if (action == UNREGISTER) {
            if (receiverRunning)
                unregisterReceiver(serviceBroadcastReceiver);
            receiverRunning = false;
        }
    }
    private void programAlert(@Nullable Integer pos) {
        int size = programs.list().size();
        int s = (pos == null ? size : pos);
        boolean update = s < size;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        ConstraintLayout cl = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.alert_program, null);
        EditText name = cl.findViewById(R.id.name);
        EditText desc = cl.findViewById(R.id.desc);

        if (update) {
            name.setText(programs.list().get(pos).getName());
            desc.setText(programs.list().get(pos).getDescription());
        }

        builder.setView(cl);

        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            String nameString = name.getText().toString();
            String descString = desc.getText().toString();

            if (update) {
                programs.list().get(pos).setName(nameString);
                programs.list().get(pos).setDescription(descString);
            } else {
                Program p = new Program();
                p.setName(nameString);
                p.setDescription(descString);
                programs.addProgram(p);
            }
            programs.save();
            runOnUiThread(() -> programAdapter.notifyDataSetChanged());
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {

        });

        builder.show();
    }

    @Override
    public void onItemClick(int pos) {
        loadProgram(programs.list().get(pos).getName());
    }

    @Override
    public void onItemEdit(int pos) {
        programAlert(pos);
    }

    @Override
    public void onItemLongClick(int pos) {
        DeleteAlert.delete(this, programs.list().get(pos), this);
    }

    @Override
    public void onItemDeleted() {
        programAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = menuItemSelector(this, item);
        return result || super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver(REGISTER);
        service(StatusService.START_SERVICE);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        service(StatusService.STOP_SERVICE);
        receiver(UNREGISTER);
        super.onDestroy();
    }
}