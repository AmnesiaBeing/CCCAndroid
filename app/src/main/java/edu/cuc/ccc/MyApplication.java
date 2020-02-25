package edu.cuc.ccc;

import android.app.Application;
import android.content.Context;

import edu.cuc.ccc.backends.DeviceManager;
import edu.cuc.ccc.backends.NSDHandler;
import edu.cuc.ccc.plugins.PluginFactory;

import static edu.cuc.ccc.utils.DeviceUtil.generateMyInfo;
import static edu.cuc.ccc.utils.DeviceUtil.loadMyInfo;

public class MyApplication extends Application {

    public static Context appContext;

    // 20200221重新思考了程序的加载顺序
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        new DeviceManager();
        if (MySharedPreferences.isFirstRun(this)) {
            try {
                generateMyInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                loadMyInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        new NSDHandler().startNSDHandler();
    }
}
