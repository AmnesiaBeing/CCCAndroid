package edu.cuc.ccc.plugins;

import org.atteo.classindex.ClassIndex;
import org.atteo.classindex.IndexAnnotated;

import java.util.HashMap;
import java.util.Map;

public class PluginFactory {

    private static final String TAG = PluginFactory.class.getSimpleName();

    // 所有可加载的插件都会自动地放到这个接口里
    @IndexAnnotated
    public @interface LoadablePlugins {
    }

    // 使用kv数据结构存储加载好的插件，把名称和插件类对应起来
    private static final Map<String, Plugin> pluginInfo = new HashMap<>();

    public static Map<String, Plugin> getPlugins() {
        return pluginInfo;
    }

    public static void initPluginInfo(String uuid) {
        for (Class<?> pluginClass : ClassIndex.getAnnotated(LoadablePlugins.class)) {
            Plugin plugin;
            try {
                // newInstance()相当于弱化版的new
                plugin = ((Plugin) pluginClass.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            pluginInfo.put(plugin.getPluginName(), plugin);
        }
    }

    public static void pluginExecute(String pluginName) {
        Plugin plugin = pluginInfo.get(pluginName);
        if (plugin != null) plugin.pluginExecute();
    }
}
