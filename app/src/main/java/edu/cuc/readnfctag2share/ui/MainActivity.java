package edu.cuc.readnfctag2share.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import edu.cuc.readnfctag2share.R;
import edu.cuc.readnfctag2share.backends.BackendService;
import edu.cuc.readnfctag2share.helpers.BackendServiceHelper;
import edu.cuc.readnfctag2share.helpers.SharedPreferencesHelper;


public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, CheckBox.OnCheckedChangeListener, BackendService.BackendServiceCallbackInterface {

    private static String TAG = MainActivity.class.getSimpleName();

    private BackendServiceHelper backendServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        backendServiceHelper = new BackendServiceHelper(this);
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        backendServiceHelper.onStart();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop");
        backendServiceHelper.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (isFinishing()) {
            backendServiceHelper.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void BackendServiceCallback() {
        Log.i(TAG, "BackendServiceCallback");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.plus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_add:
//                startActivity(new Intent(this, NewTAGActivity.class));
                Intent intent = new Intent(this, BackendService.class);
                intent.putExtra("command", "aaa");
                startService(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        SharedPreferencesHelper.getApplicationSharedPreferences().edit().putInt("TransMethod", TransMethodID2Index(checkedId)).apply();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String key = null;
        switch (buttonView.getId()) {
            case R.id.cb_send_clipboard:
                key = "SendClipboard";
                break;
            case R.id.cb_recv_clipboard:
                key = "RecvClipboard";
                break;
        }
        if (key != null)
            SharedPreferencesHelper.getApplicationSharedPreferences().edit().putBoolean(key, isChecked).apply();
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        CheckBox cbSendClipboard = findViewById(R.id.cb_send_clipboard);
        cbSendClipboard.setOnCheckedChangeListener(this);
        cbSendClipboard.setChecked(SharedPreferencesHelper.getApplicationSharedPreferences().getBoolean("SendClipboard", true));
        CheckBox cbRecvClipboard = findViewById(R.id.cb_recv_clipboard);
        cbRecvClipboard.setOnCheckedChangeListener(this);
        cbRecvClipboard.setChecked(SharedPreferencesHelper.getApplicationSharedPreferences().getBoolean("RecvClipboard", true));
        RadioGroup rgTransMethod = findViewById(R.id.rg_trans_method);
        rgTransMethod.setOnCheckedChangeListener(this);
        // 好长
        ((RadioButton) findViewById(
                TransMethodIndex2ID(
                        SharedPreferencesHelper.getApplicationSharedPreferences().getInt("TransMethod", 0))))
                .setChecked(true);
    }

    private int TransMethodID2Index(int id) {
        int value = 0;
        switch (id) {
            case R.id.rb_trans_method_auto:
                value = 0;
                break;
            case R.id.rb_trans_method_wlan:
                value = 1;
                break;
            case R.id.rb_trans_method_p2p:
                value = 2;
                break;
            case R.id.rb_trans_method_bt:
                value = 3;
                break;
        }
        return value;
    }

    private int TransMethodIndex2ID(int value) {
        final int[] map = new int[]{
                R.id.rb_trans_method_auto, R.id.rb_trans_method_wlan,
                R.id.rb_trans_method_p2p, R.id.rb_trans_method_bt};
        if (value > map.length || value < 0) value = 0;
        return map[value];
    }

}
