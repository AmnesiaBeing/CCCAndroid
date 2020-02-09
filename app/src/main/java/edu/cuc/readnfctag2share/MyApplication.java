package edu.cuc.readnfctag2share;

import android.app.Application;

import edu.cuc.readnfctag2share.backends.BackendService;
import edu.cuc.readnfctag2share.helpers.BackendServiceHelper;
import edu.cuc.readnfctag2share.helpers.SharedPreferencesHelper;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesHelper.initInstant(this);
        BackendService.startBackendService(this);
    }
}
