package rnfive.htfu.temperatureregulator.define;

import android.app.AlertDialog;
import android.content.Context;

import rnfive.htfu.temperatureregulator.R;

import static rnfive.htfu.temperatureregulator.MainActivity.programs;

public final class DeleteAlert {
    private DeleteAlert() {}

    public static <T> void delete(Context context, T t, OnItemClickListener listener) {
        String message;
        boolean isProgram = false;
        if (t instanceof Program) {
            message = "Program \"" + ((Program) t).getName() + "\"?";
            isProgram = true;
        } else if (t instanceof Program.Step) {
            message = "Step \"" + (((Program.Step) t).getId() + 1) + "\"?";
        } else {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.delete));
        builder.setMessage(message);

        boolean isP = isProgram;
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            if (isP) {
                programs.removeProgram((Program) t);
            } else {
                Program.Step s = (Program.Step) t;
                programs.list().get(s.getProgramPos()).removeStep(s);
            }
            programs.save();
            listener.onItemDeleted();
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {

        });

        builder.show();
    }
}
