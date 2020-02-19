package edu.cuc.ccc.backends;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.DeviceUtil;
import edu.cuc.ccc.MyApplication;
import edu.cuc.ccc.R;
import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

// 这个类主要负责SSL相关的东西
/*
认证流程：（规划）
0、C首先发送的是pair请求
1、C发送连接请求，获取S的证书与公钥（由CA的私钥加密而得），如果通过二维码得知S的存在，则可以从二维码中获得pin码
2-1、首先判断S的证书是否符合要求（自签名证书），如果不是，该服务器不是本程序所需要的服务器
3、C判断自身是否存储有S的证书与公钥，即是否曾经连接过S
3-1、如果是，使用证书解密得到公钥，与储存的公钥对比，如果不同，通知用户作出决定，相同则C信任S
4、C将自身的证书与公钥，以及pin码(get-url)，发送给S
5、S判断是否已有证书，如果已有证书，S信任C，认证过程完成，pair请求返回需要执行的动作
6、S判断pin码是否二维码生成，如果是，记录证书，认证过程完毕
6-1、如果不是，S返回需要再次认证的消息，并且S弹出提示，要求用户确认，确认并记录
参考资料：https://blog.csdn.net/dtlscsl/article/details/50118225
*/

class RPCUtil {

    // 由于目前软件使用自签名证书，所以，将服务器中的证书存储在设备中
    private static X509Certificate getX509SystemCertificate() throws CertificateException {
        return (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(MyApplication.appContext.getResources().openRawResource(R.raw.server));
    }

    // Java使用KeyTool管理各种密钥
    private static KeyStore getKeyStore(Device targetDevice) throws GeneralSecurityException, IOException {
        // 使用java的KetTool密钥管理器管理密钥，这里将存储所有使用过的证书
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);

        // 添加自己的私钥和证书
        keyStore.setKeyEntry("key",
                DeviceManager.getInstance().getMyDevice().getPrivateKey(),
                "".toCharArray(),
                new Certificate[]{DeviceManager.getInstance().getMyDevice().getCertificate()});

        // 添加自签名证书
        X509Certificate systemCertificate = getX509SystemCertificate();
        if (systemCertificate != null) {
            keyStore.setCertificateEntry(targetDevice.getUUID(), systemCertificate);
        }

        return keyStore;
    }

    // TrustManager决定服务器是否值得信任
    private static TrustManager[] getTrustManagers(Device targetDevice) throws GeneralSecurityException, IOException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(getKeyStore(targetDevice));
        return trustManagerFactory.getTrustManagers();
    }

    // KeyManager决定将哪一个证书发送给对端服务器
    private static KeyManager[] getKeyManagers(Device targetDevice) throws GeneralSecurityException, IOException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(getKeyStore(targetDevice), "".toCharArray());
        return keyManagerFactory.getKeyManagers();
    }

    // SSL验证的上下文，提供所需要的证书什么的
    private static SSLContext getSslContext(Device targetDevice) {
        try {
            SSLContext tlsContext = SSLContext.getInstance("TLS");
            tlsContext.init(getKeyManagers(targetDevice), getTrustManagers(targetDevice), new SecureRandom());

            return tlsContext;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static HttpUrl getHttpUrl(Device targetDevice, Map<String, String> query) {
        String targetHost = targetDevice.getName() + ".local";
        int targetPort = targetDevice.getIPPortAddress().get(0).getPort();
        HttpUrl.Builder builder = new HttpUrl.Builder()
                .scheme("https")
                .host(targetHost)
                .port(targetPort);
        for (Map.Entry<String, String> entry : query.entrySet()) {
            builder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    static OkHttpClient getOkHttpClient(Device targetDevice) {
        try {
            return new OkHttpClient.Builder()
                    .readTimeout(5, TimeUnit.SECONDS)
                    .hostnameVerifier((hostname, session) -> hostname.equals(targetDevice.getName() + ".local"))
                    .dns(getLocalDns(targetDevice))
                    .sslSocketFactory(Objects.requireNonNull(getSslContext(targetDevice)).getSocketFactory(), (X509TrustManager) getTrustManagers(targetDevice)[0])
                    .build();
        } catch (Exception e) {
            // TODO:针对不同错误作出处理，比如重新生成密钥和证书
            e.printStackTrace();
        }
        return null;
    }

    private static Dns getLocalDns(Device targetDevice) {
        return s -> {
            if (s.equals(targetDevice.getName() + ".local")) {
                List<InetAddress> ret = new ArrayList<>();
                List<DeviceUtil.IPPortAddr> tmp = targetDevice.getIPPortAddress();
                for (DeviceUtil.IPPortAddr p : tmp) {
                    ret.add(p.getAddr());
                }
                return ret;
            } else {
                throw new UnknownHostException();
            }
        };
    }

    // post
    static Request getRequest(HttpUrl url, RequestBody postData) {
        return new Request.Builder().url(url).post(postData).build();
    }

    // get
    static Request getRequest(HttpUrl url) {
        return new Request.Builder().url(url).get().build();
    }
}
