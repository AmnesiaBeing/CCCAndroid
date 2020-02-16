package edu.cuc.ccc.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import edu.cuc.ccc.R;
import edu.cuc.ccc.backends.BackendService;
import edu.cuc.ccc.backends.RPCHandler;
import edu.cuc.ccc.plugins.clipboardplugin.ClipboardHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class NDefTriggerActivity extends AppCompatActivity {

    private static final String TAG = NDefTriggerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ndef_trigger);

        Intent intent = getIntent();
        Log.i(TAG, "onCreate: " + intent);

        BackendService.getInstance().getRpcHandler().sendRPCRequest(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Log.i(TAG, "onFailure");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.i(TAG, "onResponse");
                ClipboardHelper.putClipboardContent(NDefTriggerActivity.this, response.body().string());
            }
        });
    }

}
