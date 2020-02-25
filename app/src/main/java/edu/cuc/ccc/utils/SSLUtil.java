package edu.cuc.ccc.utils;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import edu.cuc.ccc.Device;
import edu.cuc.ccc.backends.DeviceManager;

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

public class SSLUtil {

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

        if (targetDevice.isTrusted()) {
            // 添加服务端证书
            Certificate certificate = targetDevice.getCertificate();
            if (certificate != null) {
                keyStore.setCertificateEntry(targetDevice.getUUID(), certificate);
            }
        }

        return keyStore;
    }

    // TrustManager决定服务器是否值得信任
    private static TrustManager[] getTrustManagers(Device targetDevice) throws GeneralSecurityException, IOException {
        // 第一次用的时候肯定是不信任的，无所谓了
        if (targetDevice.isTrusted()) {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(getKeyStore(targetDevice));
            return trustManagerFactory.getTrustManagers();
        } else {
            X509TrustManager TrustAllCertificate = new X509TrustManager() {
                @SuppressLint("TrustAllX509TrustManager")
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                // 返回所有证书
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            return new TrustManager[]{TrustAllCertificate};
        }
    }

    // KeyManager决定将哪一个证书发送给对端服务器
    private static KeyManager[] getKeyManagers(Device targetDevice) throws
            GeneralSecurityException, IOException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(getKeyStore(targetDevice), "".toCharArray());

        return keyManagerFactory.getKeyManagers();
    }

    // SSL验证的上下文，提供所需要的证书什么的
    public static SSLContext getSslContext(Device targetDevice) throws
            GeneralSecurityException, IOException {
        SSLContext tlsContext = SSLContext.getInstance("TLS");
        tlsContext.init(getKeyManagers(targetDevice), getTrustManagers(targetDevice), new SecureRandom());

        return tlsContext;
    }
}
