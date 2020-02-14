package edu.cuc.ccc.plugins;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import edu.cuc.ccc.backends.BackendService;

public abstract class PluginBase {
    // 能够实现每个类自行生成这个TAG字符串嘛？
    private static final String TAG = PluginBase.class.getSimpleName();

    protected BackendService backedService;
    protected SharedPreferences sharedPreferences;
    protected Context applicationContext;
    protected String name;

    public final void setContext(Context context) {
        applicationContext = context;
    }

    public final void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public final void setBackendService(BackendService service) {
        this.backedService = service;
    }

    // 插件名称
    public abstract String getPluginName();

    // 是否在主界面显示一个插件的View
    public boolean hasViewInMainActivity() {
        return false;
    }

    public View getViewInMainActivity(Context context) {
        return null;
    }


}
