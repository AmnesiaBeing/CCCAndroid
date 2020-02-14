package edu.cuc.ccc;

import android.content.Context;
import android.content.SharedPreferences;

import edu.cuc.ccc.R;

public class MySharedPreferences {

    private static SharedPreferences sharedPreferences;

    public static SharedPreferences getApplicationSharedPreferences() {
        return sharedPreferences;
    }

    public static void initInstant(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        }
    }
}
