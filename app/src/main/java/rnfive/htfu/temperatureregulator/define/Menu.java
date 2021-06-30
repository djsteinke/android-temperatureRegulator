package rnfive.htfu.temperatureregulator.define;

import android.view.MenuItem;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import rnfive.htfu.temperatureregulator.R;
import rnfive.htfu.temperatureregulator.UrlListener;

import static rnfive.htfu.temperatureregulator.MainActivity.programs;
import static rnfive.htfu.temperatureregulator.define.Constants.getJsonString;

public final class Menu {

    private static final int shutdown = R.id.shutdown;
    private static final int restart = R.id.restart;
    private static final int upload = R.id.upload;

    private Menu() {
    }

    public static boolean menuItemSelector(UrlListener listener, MenuItem item) {
        Executor executor = Executors.newSingleThreadExecutor();
        switch (item.getItemId()) {
            case shutdown:
                executor.execute(new UrlRunnable(listener, "pi/h"));
                break;
            case restart:
                executor.execute(new UrlRunnable(listener, "pi/r"));
                break;
            case upload:
                String msg = getJsonString(programs);
                executor.execute(new UrlPostRunnable(listener, "upload", msg));
                break;
            default:
                break;
        }
        return true;
    }

}
