package rnfive.htfu.temperatureregulator.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import rnfive.htfu.temperatureregulator.R;
import rnfive.htfu.temperatureregulator.define.OnItemClickListener;
import rnfive.htfu.temperatureregulator.define.Program;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProgramStepAdapter extends RecyclerView.Adapter<ProgramStepAdapter.MyViewHolder> {
    private static final String TAG = ProgramStepAdapter.class.getSimpleName();
    private static List<Program.Step> dataSet = new ArrayList<>();
    private final OnItemClickListener listener;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private int id;
        final View vItem;
        MyViewHolder(View v) {
            super(v);
            vItem = v;
        }
        public void setId(int id) {
            this.id = id;
        }
        public int getId() {
            return id;
        }
        public TextView getTextView() {
            return vItem.findViewById(R.id.name_text);
        }
        public TextView getDesc() {
            return vItem.findViewById(R.id.desc_text);
        }
    }

    public ProgramStepAdapter(List<Program.Step> inDataSet, OnItemClickListener listener) {
        this.listener = listener;
        dataSet = inDataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override @NonNull
    public ProgramStepAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_step, viewGroup, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        view.setOnClickListener(click(myViewHolder));
        view.setOnLongClickListener(longClick(myViewHolder));
        AppCompatImageButton btEdit = view.findViewById(R.id.edit);
        btEdit.setVisibility(View.GONE);
        //btEdit.setOnClickListener(click(myViewHolder));
        return myViewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder viewHolder, final int position) {
        Log.d(TAG, "onBindViewHolder()[" + position + "/" + (getItemCount()-1) + "]");
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Program.Step s = dataSet.get(position);
        String txt = "S: " + (s.getId()+1);
        String desc = s.getTime() + " min @ " + s.getTemperature() + "\u00B0C";
        desc += "\nVacuum: " + (s.isVacuum() ? "ON" : "OFF");

        viewHolder.getTextView().setText(txt);
        viewHolder.getDesc().setText(desc);
        viewHolder.setId(position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public View.OnClickListener click(MyViewHolder myViewHolder) {
        return v -> listener.onItemClick(myViewHolder.getId());
    }
    public View.OnLongClickListener longClick(MyViewHolder myViewHolder) {
        return v -> {
            listener.onItemLongClick(myViewHolder.getId());
            return false;
        };
    }

}

