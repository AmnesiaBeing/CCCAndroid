package edu.cuc.ccc;

import android.app.Application;
import android.content.Context;

import edu.cuc.ccc.backends.BackendService;
import edu.cuc.ccc.plugins.PluginFactory;

public class MyApplication extends Application {

    public static Context appContext;

    @Override
    public void onCreate() {
        appContext = this;
        super.onCreate();
        BackendService.startBackendService(this);
        MySharedPreferences.initInstant(this);
        PluginFactory.initPluginInfo(this);
    }
}
