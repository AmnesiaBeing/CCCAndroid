package edu.cuc.ccc.plugins;

import android.content.Context;
import android.view.View;

import edu.cuc.ccc.Device;

public abstract class Plugin {
    // 能够实现每个类自行生成这个TAG字符串嘛？
    private static final String TAG = Plugin.class.getSimpleName();

    protected String name;

    protected Device targetDevice;

    // 插件名称
    public abstract String getPluginName();

    // 是否在主界面显示一个插件的View
    public boolean hasViewInMainActivity() {
        return false;
    }

    public View getViewInMainActivity(Context context) {
        return null;
    }

    public abstract void pluginExecute();
}
