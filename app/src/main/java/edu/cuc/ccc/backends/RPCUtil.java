package edu.cuc.ccc.backends;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.DeviceUtil;
import edu.cuc.ccc.MySharedPreferences;
import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

// 这个类主要负责SSL相关的东西
public class RPCUtil {

    public static void initialiseRsaKeys() {
        SharedPreferences settings = MySharedPreferences.getApplicationSharedPreferences();

        if (!settings.contains("publicKey") || !settings.contains("privateKey")) {

            KeyPair keyPair;
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(2048);
                keyPair = keyGen.genKeyPair();
            } catch (Exception e) {
                Log.e("KDE/initializeRsaKeys", "Exception", e);
                return;
            }

            byte[] publicKey = keyPair.getPublic().getEncoded();
            byte[] privateKey = keyPair.getPrivate().getEncoded();

            SharedPreferences.Editor edit = settings.edit();
            edit.putString("publicKey", Base64.encodeToString(publicKey, 0).trim() + "\n");
            edit.putString("privateKey", Base64.encodeToString(privateKey, 0));
            edit.apply();
        }
    }

    public static PublicKey getPublicKey() throws GeneralSecurityException {
        SharedPreferences settings = MySharedPreferences.getApplicationSharedPreferences();
        byte[] publicKeyBytes = Base64.decode(settings.getString("publicKey", ""), 0);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    }

    public static PrivateKey getPrivateKey() throws GeneralSecurityException {
        SharedPreferences settings = MySharedPreferences.getApplicationSharedPreferences();
        byte[] privateKeyBytes = Base64.decode(settings.getString("privateKey", ""), 0);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
    }

    private static KeyStore getKeyStore(Device targetDevice) {
        try {
            // 使用java的KetTool密钥管理器管理密钥
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            X509Certificate remoteDeviceCertificate = getX509CertificateFromString(targetDevice.getCertificate());

            // 如果是已配对设备，将它的证书也存进来
            if (remoteDeviceCertificate != null) {
                keyStore.setCertificateEntry(targetDevice.getDeviceUUID(), remoteDeviceCertificate);
            }
            return keyStore;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static X509Certificate getX509CertificateFromString(String certString) throws CertificateException {
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certString.getBytes()));
    }

//    private static X509TrustManager[] getTrustManagers(Device targetDevice) {
//        if (targetDevice.isPaired()) {
//            try {
//                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//                trustManagerFactory.init(getKeyStore(targetDevice));
//                return (X509TrustManager[]) trustManagerFactory.getTrustManagers();
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//        } else {
//            // 信任所有的证书，用于未配对时，不管对方是什么，先把信息接受了再说
//            return new X509TrustManager[]{new X509TrustManager() {
//                public X509Certificate[] getAcceptedIssuers() {
//                    return new X509Certificate[0];
//                }
//
//                @SuppressLint("TrustAllX509TrustManager")
//                @Override
//                public void checkClientTrusted(X509Certificate[] certs, String authType) {
//                }
//
//                @SuppressLint("TrustAllX509TrustManager")
//                @Override
//                public void checkServerTrusted(X509Certificate[] certs, String authType) {
//                }
//            }};
//        }
//    }

    // 这部分代码还没写好，为了测试，就完全信任吧
    private static X509TrustManager[] getTrustManagers(Device targetDevice) {
        // 信任所有的证书，用于未配对时，不管对方是什么，先把信息接受了再说
        return new X509TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};
    }

    private static SSLContext getSslContext(Device targetDevice) {
        try {
            // Setup key manager factory
//            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//            keyManagerFactory.init(getKeyStore(targetDevice), "".toCharArray());

            SSLContext tlsContext = SSLContext.getInstance("TLS");
//            tlsContext.init(keyManagerFactory.getKeyManagers(), getTrustManagers(targetDevice), new SecureRandom());
            tlsContext.init(null, getTrustManagers(targetDevice), new SecureRandom());

            return tlsContext;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static HttpUrl getHttpUrl(Device targetDevice) {
        String targetHost = targetDevice.getDeviceName() + ".local";
        int targetPort = targetDevice.getDeviceIPPortAddress().get(0).getPort();
        return new HttpUrl.Builder()
                .scheme("https")
                .host(targetHost)
                .port(targetPort)
                .build();
    }

    static OkHttpClient getOkHttpClient(Device targetDevice) {
        try {
            return new OkHttpClient.Builder()
                    .readTimeout(5, TimeUnit.SECONDS)
                    .hostnameVerifier((hostname, session) -> true)
                    .dns(getLocalDns(targetDevice))
                    .sslSocketFactory(Objects.requireNonNull(getSslContext(targetDevice)).getSocketFactory(), getTrustManagers(targetDevice)[0])
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Dns getLocalDns(Device targetDevice) {
        return s -> {
            if (s.equals(targetDevice.getDeviceName() + ".local")) {
                return targetDevice.getDeviceIPPortAddress().stream().map(DeviceUtil.IPPortAddr::getAddr).collect(Collectors.toList());
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
