package edu.cuc.readnfctag2share.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import edu.cuc.readnfctag2share.R;

public class SharedPreferencesHelper {

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
