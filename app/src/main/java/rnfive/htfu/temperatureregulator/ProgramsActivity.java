package rnfive.htfu.temperatureregulator;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import rnfive.htfu.temperatureregulator.adapter.ProgramStepAdapter;
import rnfive.htfu.temperatureregulator.define.DeleteAlert;
import rnfive.htfu.temperatureregulator.define.OnItemClickListener;
import rnfive.htfu.temperatureregulator.define.Program;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static rnfive.htfu.temperatureregulator.MainActivity.programs;

public class ProgramsActivity extends AppCompatActivity implements OnItemClickListener {
    private static final String TAG = ProgramsActivity.class.getSimpleName();
    public static final String INTENT_BUNDLE = "rnfive.htfu.temperatureregulator.ProgramsActivity.INTENT_BUNDLE";
    public static final String INTENT_NAME = "rnfive.htfu.temperatureregulator.ProgramsActivity.INTENT_NAME";

    private Program program;
    private TextView name;
    private TextView desc;
    private int pPos;
    private final List<Program.Step> steps = new ArrayList<>();
    private ProgramStepAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programs);

        name = findViewById(R.id.program_name);
        desc = findViewById(R.id.program_desc);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(addStepClick());
        AppCompatImageButton ibAddStep = findViewById(R.id.ib_add_step);
        ibAddStep.setOnClickListener(addStepClick());
        RecyclerView rv = findViewById(R.id.recycler_view);

        rv.setHasFixedSize(true);
        adapter = new ProgramStepAdapter(steps, this);
        rv.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(INTENT_BUNDLE);
        if (bundle != null) {
            String name = bundle.getString(INTENT_NAME);
            Log.d(TAG, "bundle Name(" + name + ")");
            for (Program p : programs.list()) {
                if (p.getName().equals(name)) {
                    pPos = programs.list().indexOf(p);
                    setProgram();
                    Log.d(TAG, "program " + program.toString());
                    break;
                }
            }
        } else {
            program = new Program();
            loadSteps(program.getSteps());
        }
    }

    private View.OnClickListener addStepClick() {
        return v -> addStepAlert(null);
    }

    private void setProgram() {
        program = programs.list().get(pPos);
        loadSteps(program.getSteps());
        adapter.notifyDataSetChanged();
        name.setText(program.getName());
        desc.setText(program.getDescription());
    }

    private void loadSteps(List<Program.Step> inSteps) {
        steps.clear();
        steps.addAll(inSteps);
    }

    private void addStepAlert(@Nullable Integer pos) {

        int s = (pos == null ? steps.size() : pos);
        boolean update = s < steps.size();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        ConstraintLayout cl = (ConstraintLayout) LayoutInflater.from(this).inflate(R.layout.alert_step, null);
        TextView step_id = cl.findViewById(R.id.step_id);
        EditText time = cl.findViewById(R.id.time);
        EditText temp = cl.findViewById(R.id.temp);
        SwitchCompat vacuum = cl.findViewById(R.id.sw_vacuum);

        int tp1 = (update ? steps.get(pos).getTemperature() : 25);
        int tm1 = (update ? steps.get(pos).getTime() : 0);
        String strStep = "Step " + (s+1);
        step_id.setText(strStep);
        time.setText(String.valueOf(tm1));
        temp.setText(String.valueOf(tp1));
        vacuum.setChecked((update && steps.get(pos).isVacuum()));

        builder.setView(cl);

        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            int tm = Integer.parseInt(time.getText().toString());
            int tp = Integer.parseInt(temp.getText().toString());
            boolean vac = vacuum.isChecked();

            Program.Step step = new Program.Step(s, tp, 0, tm, vac, pPos);
            if (update) {
                steps.set(pos, step);
                programs.list().get(pPos).updateStep(step);
            } else {
                steps.add(step);
                programs.list().get(pPos).addStep(step);
            }
            programs.save();
            runOnUiThread(() -> adapter.notifyDataSetChanged());
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {

        });

        builder.show();
    }

    @Override
    public void onItemClick(int pos) {
        addStepAlert(pos);
    }
    @Override
    public void onItemLongClick(int pos) {
        DeleteAlert.delete(this, program.getSteps().get(pos), this);
    }
    @Override
    public void onItemDeleted() {
        setProgram();
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onItemEdit(int id) {

    }
}
