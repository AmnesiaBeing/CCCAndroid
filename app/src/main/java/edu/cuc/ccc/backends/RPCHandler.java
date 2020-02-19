package edu.cuc.ccc.backends;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.plugins.PluginBase;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

import static edu.cuc.ccc.backends.RPCUtil.getOkHttpClient;
import static edu.cuc.ccc.backends.RPCUtil.getRequest;

// 从Android 9开始，应用内不允许使用无SSL连接，无奈
public class RPCHandler {

    private static final String TAG = RPCHandler.class.getSimpleName();

    // 配对设备地址已知，自身信息已知，插件名称需提供，内容需要提供
    public void sendRPCRequest(Device targetDevice, String rpc, Map<String, String> query, Callback callback) {
        OkHttpClient okHttpClient = getOkHttpClient(targetDevice);
        HttpUrl url = RPCUtil.getHttpUrl(targetDevice, query);

        Request request = getRequest(url);
        Call call;
        if (okHttpClient != null) {
            call = okHttpClient.newCall(request);
            call.enqueue(callback);
        } else {
            callback.onFailure(null, null);
        }
    }

    public void sendRPCRequest(Device targetDevice, PluginBase plugin, Map<String, String> query, Callback callback) {
        sendRPCRequest(targetDevice, plugin.getPluginName(), query, callback);
    }

}
