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

    public static SharedPreferences getApplicationSharedPreferences() {
        return MyApplication.appContext.getSharedPreferences(MyApplication.appContext.getString(R.string.KEY_PREF), Context.MODE_PRIVATE);
    }

    public static SharedPreferences getSharedPreferences(String uuid) {
        return MyApplication.appContext.getSharedPreferences(uuid, Context.MODE_PRIVATE);
    }
}
