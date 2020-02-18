package edu.cuc.ccc;

import android.content.Context;
import android.content.SharedPreferences;

import static edu.cuc.ccc.MyApplication.appContext;

public class MySharedPreferences {

    private static SharedPreferences sharedPreferences;

    public static SharedPreferences getApplicationSharedPreferences() {
        return sharedPreferences;
    }

    public static SharedPreferences getSharedPreferences(String name) {
        return appContext.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    static void initInstant(Context context) {
        appContext = context;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }
}
