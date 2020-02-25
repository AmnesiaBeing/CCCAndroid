package edu.cuc.ccc;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreferences {

    static boolean isFirstRun(Context context) {
        SharedPreferences globalSharedPreferences = context.getSharedPreferences(context.getString(R.string.KEY_PREF), Context.MODE_PRIVATE);
        String keyFirstFlag = context.getResources().getString(R.string.KEY_FIRST_FLAG);
        boolean ret = globalSharedPreferences.getBoolean(keyFirstFlag, true);
        globalSharedPreferences.edit().putBoolean(keyFirstFlag, false).apply();
        return ret;
    }

    // 获取全局的设置
    public static SharedPreferences getApplicationSharedPreferences() {
        return MyApplication.appContext.getSharedPreferences(MyApplication.appContext.getString(R.string.KEY_PREF), Context.MODE_PRIVATE);
    }

    // 获取插件或者设备自身的设置
    public static SharedPreferences getSharedPreferences(String name) {
        return MyApplication.appContext.getSharedPreferences(name, Context.MODE_PRIVATE);
    }
}
