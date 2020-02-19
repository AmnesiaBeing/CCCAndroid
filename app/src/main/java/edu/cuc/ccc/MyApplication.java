package edu.cuc.ccc;

import android.app.Application;
import android.content.Context;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import edu.cuc.ccc.backends.BackendService;
import edu.cuc.ccc.backends.DeviceManager;
import edu.cuc.ccc.plugins.PluginFactory;

import static edu.cuc.ccc.DeviceUtil.generateMyInfo;
import static edu.cuc.ccc.DeviceUtil.loadMyInfo;

public class MyApplication extends Application {

    public static Context appContext;

    // TODO:如何优化加载顺序？
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
        BackendService.startBackendService(this);
        PluginFactory.initPluginInfo();
    }
}
