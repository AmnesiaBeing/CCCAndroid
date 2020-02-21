package edu.cuc.ccc.plugins.pairplugin;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.DeviceUtil;
import edu.cuc.ccc.R;
import edu.cuc.ccc.backends.BackendService;
import edu.cuc.ccc.backends.DeviceManager;
import edu.cuc.ccc.plugins.PluginBase;
import edu.cuc.ccc.plugins.PluginFactory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static edu.cuc.ccc.MyApplication.appContext;

@PluginFactory.LoadablePlugins
public class PairPlugin extends PluginBase implements Callback {
    private static final String TAG = PairPlugin.class.getSimpleName();

    private PairRequestCallback callback;

    @Override
    public String getPluginName() {
        return "pair";
    }

    @Override
    public synchronized void process(PluginProcessCallback callback) {
        Device pairingDevice = DeviceManager.getInstance().getPairingDevice();
        if (pairingDevice == null) {
            pairingDevice = DeviceManager.getInstance().getPairedDevice();
        }
        if (pairingDevice == null) return;
        Map<String, String> query = new HashMap<>();
        query.put(appContext.getResources().getString(R.string.KEY_DEVICE_NAME), pairingDevice.getName());
        query.put(appContext.getResources().getString(R.string.KEY_DEVICE_UUID), pairingDevice.getUUID());
        query.put(appContext.getResources().getString(R.string.KEY_DEVICE_TYPE), pairingDevice.getType().name());
        if (pairingDevice.getPIN() != null)
            query.put(appContext.getResources().getString(R.string.KEY_PIN), pairingDevice.getPIN());
        BackendService.getInstance().getRpcHandler().sendRPCRequest(
                pairingDevice, this, query, this);

        this.callback = (PairRequestCallback) callback;
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        Log.e(TAG, "onFailure");
        e.printStackTrace();
        if (callback != null)
            callback.onPairRequestError();
        Device pairingDevice = DeviceManager.getInstance().getPairingDevice();
        if (pairingDevice == null) {
            pairingDevice = DeviceManager.getInstance().getPairedDevice();
        }
        if (pairingDevice == null) return;
        pairingDevice.setStatus(DeviceUtil.DeviceStatus.Unknown);
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        Log.i(TAG, "onResponse");
        // TODO:当回复后，解析返回的数据，完善设备信息，并且将状态设置为已配对
        if (response.isSuccessful()) {
            String content = Objects.requireNonNull(response.body()).string();
            try {
                JSONObject jsonObject = new JSONObject(content);
                String status = jsonObject.getString("status");
                switch (status) {
                    case "ok":
                        DeviceManager.getInstance().setPairingDevice2PairedDevice();
                        //
                        if (callback != null) {
                            callback.onPairRequestComplete();
                        }
                        break;
                    case "Unauthorized":
                        break;
                    case "fail":
                        DeviceManager.getInstance().setPairingDevice2UnknownDevice();
                        if (callback != null) {
                            callback.onPairRequestComplete();
                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public interface PairRequestCallback extends PluginProcessCallback {
        void onPairRequestError();

        void onPairRequestComplete();
    }
}
