package edu.cuc.ccc.plugins.clipboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import edu.cuc.ccc.MySharedPreferences;
import edu.cuc.ccc.R;
import edu.cuc.ccc.plugins.Plugin;
import edu.cuc.ccc.plugins.PluginFactory;
import edu.cuc.ccc.rpc.CCCGrpc;
import edu.cuc.ccc.rpc.Content;
import io.grpc.Channel;

import static edu.cuc.ccc.MyApplication.appContext;

@PluginFactory.LoadablePlugins
public class ClipboardPlugin extends Plugin {
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
        return new ClipboardView(context);
    }

    @Override
    public void pluginExecute() {
        thread.run();
    }

    private Thread thread = new Thread(() -> {
        Channel channel = targetDevice.getChannel();
        CCCGrpc.CCCBlockingStub stub = CCCGrpc.newBlockingStub(channel);
        Content send = Content.newBuilder().setContent(ClipboardUtil.getClipboardContent(appContext)).build();
        Content reply = stub.changeClipboard(send);

        ClipboardUtil.putClipboardContent(appContext, reply.getContent());
    });

    private class ClipboardView extends View implements CheckBox.OnCheckedChangeListener, Button.OnClickListener {

        public ClipboardView(Context context) {
            super(context);

            SharedPreferences sharedPreferences = MySharedPreferences.getApplicationSharedPreferences();
            View v = View.inflate(context, R.layout.plugin_clipboard_main, null);
            CheckBox cbSendClipboard = v.findViewById(R.id.cb_send_clipboard);
            cbSendClipboard.setOnCheckedChangeListener(this);
            cbSendClipboard.setChecked(sharedPreferences.getBoolean("SendClipboard", true));
            CheckBox cbRecvClipboard = v.findViewById(R.id.cb_recv_clipboard);
            cbRecvClipboard.setOnCheckedChangeListener(this);
            cbRecvClipboard.setChecked(sharedPreferences.getBoolean("RecvClipboard", true));
            v.findViewById(R.id.btn_share_clipboard).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            thread.run();
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
                SharedPreferences sharedPreferences = MySharedPreferences.getSharedPreferences(ClipboardPlugin.this.getPluginName());
                sharedPreferences.edit().putBoolean(key, isChecked).apply();
            }
        }
    }

}
