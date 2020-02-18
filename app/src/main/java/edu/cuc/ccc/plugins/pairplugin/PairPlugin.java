package edu.cuc.ccc.plugins.pairplugin;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.DeviceUtil;
import edu.cuc.ccc.backends.BackendService;
import edu.cuc.ccc.backends.DeviceManager;
import edu.cuc.ccc.plugins.PluginBase;
import edu.cuc.ccc.plugins.PluginFactory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@PluginFactory.LoadablePlugins
public class PairPlugin extends PluginBase implements Callback {
    private static final String TAG = PairPlugin.class.getSimpleName();

    private PluginProcessCallback callback;

    private Device pairingDevice;

    @Override
    public String getPluginName() {
        return "pair";
    }

    @Override
    public void process(PluginProcessCallback callback) {
        Device targetDevice;
        pairingDevice = BackendService.getInstance().getDeviceManager().getPairingDevice();
        if (pairingDevice != null) {
            targetDevice = pairingDevice;
        } else {
            targetDevice = BackendService.getInstance().getDeviceManager().getPairedDevice();
        }
        if (targetDevice == null) return;
        BackendService.getInstance().getRpcHandler().sendRPCRequest(
                targetDevice,
                this, this);

        this.callback = callback;
    }


    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        Log.e(TAG, "onFailure");
        e.printStackTrace();
        if (callback != null)
            callback.PluginProcessComplete(this, "网络错误。");
        pairingDevice.setStatus(DeviceUtil.DeviceStatus.Unknown);
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        Log.i(TAG, "onResponse");
        // 当回复后，解析返回的数据，完善设备信息，并且将状态设置为已配对
        if (callback != null) {
            BackendService.getInstance().getDeviceManager().setPairingDevice2PairedDevice(pairingDevice);
            callback.PluginProcessComplete(this, "匹配成功。");
        }
    }
}
