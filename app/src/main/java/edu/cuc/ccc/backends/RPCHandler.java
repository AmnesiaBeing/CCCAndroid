package edu.cuc.ccc.backends;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import edu.cuc.ccc.Device;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// 从Android 9开始，应用内不允许使用无SSL连接，无奈
public class RPCHandler {

    private static final String TAG = RPCHandler.class.getSimpleName();

    private Device myDevice;

    public void onCreate() {
        myDevice = BackendService.getInstance().getDeviceManager().getMyDevice();
    }

    // send:get ./pair?id=myid&ts=timestamp&dn=devname&dy=devtype&
    // recv:{"status":"ok/fail","id":targetid,"reason":"xxx"}
    public void sendPairRequest(Device targetDevice, PairRequestCallback callback) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, SSLUtil.trustManagers, new java.security.SecureRandom());

            OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            }).sslSocketFactory(sslContext.getSocketFactory(), (X509ExtendedTrustManager) SSLUtil.trustManagers[0]).build();
            HttpUrl url = new HttpUrl.Builder().scheme("https").host("192.168.1.4").port(8888).addQueryParameter("id", "123456").build();
            Request request = new Request.Builder().url(url).get().build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // TODO:应该咋抽象呢？
    public void sendRPCRequest(Callback callback) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, SSLUtil.trustManagers, new java.security.SecureRandom());

            OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            }).sslSocketFactory(sslContext.getSocketFactory(), (X509ExtendedTrustManager) SSLUtil.trustManagers[0]).build();
            HttpUrl url = new HttpUrl.Builder().scheme("https").host("192.168.1.4").port(8888).addQueryParameter("id", "123456").build();
            Request request = new Request.Builder().url(url).get().build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static abstract class PairRequestCallback implements Callback {
        public abstract void onPairRequestError();

        public abstract void onPairRequestComplete();

        @Override
        public final void onFailure(@NotNull Call call, @NotNull IOException e) {
            e.printStackTrace();
            onPairRequestError();
        }

        @Override
        public final void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            onPairRequestComplete();
        }
    }

}
