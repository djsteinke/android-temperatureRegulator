package rnfive.htfu.temperatureregulator.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;
import rnfive.htfu.temperatureregulator.R;
import rnfive.htfu.temperatureregulator.define.OnItemClickListener;
import rnfive.htfu.temperatureregulator.define.Program;

public class ProgramAdapter extends RecyclerView.Adapter<ProgramAdapter.MyViewHolder> {
    private static final String TAG = ProgramStepAdapter.class.getSimpleName();
    private static List<Program> dataSet = new ArrayList<>();
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

    public ProgramAdapter(List<Program> inDataSet, OnItemClickListener listener) {
        this.listener = listener;
        dataSet = inDataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override @NonNull
    public ProgramAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_step, viewGroup, false);

        ProgramAdapter.MyViewHolder myViewHolder = new ProgramAdapter.MyViewHolder(view);
        view.setOnClickListener(click(myViewHolder));
        view.setOnLongClickListener(longClick(myViewHolder));
        AppCompatImageButton btEdit = view.findViewById(R.id.edit);
        btEdit.setOnClickListener(edit(myViewHolder.getId()));
        return myViewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ProgramAdapter.MyViewHolder viewHolder, final int position) {
        Log.d(TAG, "onBindViewHolder()[" + position + "/" + (getItemCount()-1) + "]");
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        Program p = dataSet.get(position);
        String txt = p.getName();
        if (p.getDescription() != null)
            txt += "\n" + p.getDescription();

        viewHolder.getTextView().setText(p.getName());
        viewHolder.getDesc().setText(p.getDescription());
        viewHolder.setId(position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public View.OnClickListener click(ProgramAdapter.MyViewHolder myViewHolder) {
        return v -> listener.onItemClick(myViewHolder.getId());
    }

    public View.OnClickListener edit(int id) {
        return v -> listener.onItemEdit(id);
    }

    public View.OnLongClickListener longClick(ProgramAdapter.MyViewHolder myViewHolder) {
        return v -> {
            listener.onItemLongClick(myViewHolder.getId());
            return false;
        };
    }
}
