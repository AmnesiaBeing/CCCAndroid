package edu.cuc.ccc;

import android.app.Application;

import edu.cuc.ccc.backends.BackendService;
import edu.cuc.ccc.plugins.PluginFactory;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MySharedPreferences.initInstant(this);
        BackendService.startBackendService(this);
        PluginFactory.initPluginInfo(this);
    }
}
