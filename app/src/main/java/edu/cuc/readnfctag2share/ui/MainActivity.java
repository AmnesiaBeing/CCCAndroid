package edu.cuc.readnfctag2share.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import edu.cuc.readnfctag2share.helpers.SharedPreferencesHelper;


public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, CheckBox.OnCheckedChangeListener {

    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BackendService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private boolean mBound = false;
    private BackendService mService;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BackendService.BackendServiceBinder binder = (BackendService.BackendServiceBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

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
        // 这个写法总感觉很不优雅？不知有没有类似于C的数组寻址的写法？
        int id = 0;
        switch (SharedPreferencesHelper.getApplicationSharedPreferences().getInt("TransMethod", 0)) {
            case 0:
                id = R.id.rb_trans_method_auto;
                break;
            case 1:
                id = R.id.rb_trans_method_wlan;
                break;
            case 2:
                id = R.id.rb_trans_method_p2p;
                break;
            case 3:
                id = R.id.rb_trans_method_bt;
                break;
        }
        ((RadioButton) findViewById(id)).setChecked(true);
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
                mService.test();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int value = 0;
        switch (checkedId) {
            case R.id.rb_trans_method_auto:
//              value=0;
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
        SharedPreferencesHelper.getApplicationSharedPreferences().edit().putInt("TransMethod", value).apply();
        Log.i(TAG, "onCheckedChanged Value:" + value);
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
        Log.i(TAG, "onCheckedChanged Checked:" + isChecked);
    }

    @Override
    public void onStop() {
        unbindService(connection);
        mBound = false;
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (isFinishing()) {

        }
        super.onDestroy();
    }
}
