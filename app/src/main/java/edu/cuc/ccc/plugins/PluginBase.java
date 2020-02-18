package edu.cuc.ccc.plugins;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import edu.cuc.ccc.backends.BackendService;

public abstract class PluginBase {
    // 能够实现每个类自行生成这个TAG字符串嘛？
    private static final String TAG = PluginBase.class.getSimpleName();

    protected String name;

    // 插件名称
    public abstract String getPluginName();

    // 是否在主界面显示一个插件的View
    public boolean hasViewInMainActivity() {
        return false;
    }

    public View getViewInMainActivity(Context context) {
        return null;
    }

    public abstract void process(PluginProcessCallback callback);

    public interface PluginProcessCallback {
        // 这个插件需要说点什么
        void PluginProcessMessage(PluginBase plugin, String str);

        // 这个插件执行完毕
        void PluginProcessComplete(PluginBase plugin, String str);

    }

}
