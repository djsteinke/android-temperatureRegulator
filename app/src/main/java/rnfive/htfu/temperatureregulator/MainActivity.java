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

import static rnfive.htfu.temperatureregulator.define.Menu.menuItemSelector;
import static rnfive.htfu.temperatureregulator.define.enums.Action.*;
import static rnfive.htfu.temperatureregulator.define.Constants.convertTemp;
import static rnfive.htfu.temperatureregulator.define.Constants.formatInt;
import static rnfive.htfu.temperatureregulator.define.Constants.fromJsonString;

public class MainActivity extends AppCompatActivity implements UrlListener, OnItemClickListener {

    public final static String TAG = MainActivity.class.getSimpleName();

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
            //DecimalFormat df1 = new DecimalFormat("#.#");
            DecimalFormat df0 = new DecimalFormat("#");

            response = fromJsonString(val, Response.class);
            if (response.getCode() == 400) {
                String fail = "Failed to connect to";
                if (!response.getValue().contains(fail))
                    Toast.makeText(this, "Error: " + response.getValue(), Toast.LENGTH_SHORT).show();
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

                        TextView tv = findViewById(R.id.name_text);
                        tv.setText(txt);

                        boolean h = response.getStatus().isHeatRunning() || response.getStatus().isHeatOn();

                        btHeat.setActivated(h);
                        btVacuum.setActivated(response.getStatus().isVacuumRunning());
                        btProgram.setActivated(response.getStatus().isProgramRunning());
                        drawHistory();

                        Log.d(TAG, txt);
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

    private String getTempString(double c) {
        DecimalFormat df1 = new DecimalFormat("#.#");
        return df1.format(c) + "\u00B0C [" + df1.format(convertTemp(null, c)) + "\u00B0F]";
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
        service(StatusService.STOP_SERVICE);
        receiver(UNREGISTER);
    }
}