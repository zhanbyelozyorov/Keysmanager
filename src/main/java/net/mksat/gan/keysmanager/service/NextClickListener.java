package net.mksat.gan.keysmanager.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by sergey on 6/10/14.
 */
public class NextClickListener implements View.OnClickListener {

    private Activity activity;
    Class<? extends Activity> cl;
    Bundle bundle;

    public static final String GET_DATA_BUNDLE = "data_bundle";

    public NextClickListener(Activity activity, Class<? extends Activity> cl, Bundle bundle) {
        this.activity = activity;
        this.cl = cl;
        this.bundle = bundle;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(activity, cl);
        intent.putExtra(GET_DATA_BUNDLE, bundle);
        activity.startActivity(new Intent(activity, cl));
    }
}
