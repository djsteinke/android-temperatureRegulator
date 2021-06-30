package rnfive.htfu.temperatureregulator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ServiceBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_STATUS = "rnfive.htfu.temperatureregulator.STATUS";
    private final UrlListener listener;

    public ServiceBroadcastReceiver(UrlListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getBundleExtra(StatusService.BUNDLE_ID);
        String response = bundle.getString(StatusService.INTENT_RESPONSE);
        String endPoint = bundle.getString(StatusService.INTENT_ENDPOINT);
        listener.onGetComplete(response, endPoint);
    }
}
