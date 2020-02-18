package edu.cuc.ccc.plugins.clipboardplugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import edu.cuc.ccc.MySharedPreferences;
import edu.cuc.ccc.R;
import edu.cuc.ccc.plugins.PluginBase;
import edu.cuc.ccc.plugins.PluginFactory;

@PluginFactory.LoadablePlugins
public class ClipboardPlugin extends PluginBase implements CheckBox.OnCheckedChangeListener, Button.OnClickListener {
    private static final String TAG = ClipboardPlugin.class.getSimpleName();

    @Override
    public String getPluginName() {
        return "cb";
    }

    @Override
    public boolean hasViewInMainActivity() {
        return true;
    }

    @Override
    public View getViewInMainActivity(Context context) {
        SharedPreferences sharedPreferences = MySharedPreferences.getApplicationSharedPreferences();
        View v = View.inflate(context, R.layout.plugin_clipboard_main, null);
        CheckBox cbSendClipboard = v.findViewById(R.id.cb_send_clipboard);
        cbSendClipboard.setOnCheckedChangeListener(this);
        cbSendClipboard.setChecked(sharedPreferences.getBoolean("SendClipboard", true));
        CheckBox cbRecvClipboard = v.findViewById(R.id.cb_recv_clipboard);
        cbRecvClipboard.setOnCheckedChangeListener(this);
        cbRecvClipboard.setChecked(sharedPreferences.getBoolean("RecvClipboard", true));
        v.findViewById(R.id.btn_share_clipboard).setOnClickListener(this);
        return v;
    }

    @Override
    public void process(PluginProcessCallback callback) {

    }

    @Override
    public void onClick(View v) {

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
        if (key != null) {
            SharedPreferences sharedPreferences = MySharedPreferences.getApplicationSharedPreferences();
            sharedPreferences.edit().putBoolean(key, isChecked).apply();
        }
    }
}
