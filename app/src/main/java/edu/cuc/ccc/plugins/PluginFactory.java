package edu.cuc.ccc.plugins;

import android.content.Context;

import org.atteo.classindex.ClassIndex;
import org.atteo.classindex.IndexAnnotated;

import java.util.HashMap;
import java.util.Map;

import edu.cuc.ccc.MySharedPreferences;
import edu.cuc.ccc.backends.BackendService;

public class PluginFactory {

    // 所有可加载的插件都会自动地放到这个接口里
    @IndexAnnotated
    public @interface LoadablePlugins {
    }

    // 使用kv数据结构存储加载好的插件，把名称和插件类对应起来
    private static final Map<String, PluginBase> pluginInfo = new HashMap<>();

    public static Map<String, PluginBase> getPlugins() {
        return pluginInfo;
    }

    // TODO:实现快慢加载？
    public static void initPluginInfo(Context context) {
        for (Class<?> pluginClass : ClassIndex.getAnnotated(LoadablePlugins.class)) {
            PluginBase plugin;
            try {
                // newInstance()相当于弱化版的new
                plugin = ((PluginBase) pluginClass.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            plugin.setContext(context);
            plugin.setSharedPreferences(MySharedPreferences.getApplicationSharedPreferences());
            plugin.setBackendService(BackendService.getInstance());
            pluginInfo.put(plugin.getPluginName(), plugin);
        }
    }

}
