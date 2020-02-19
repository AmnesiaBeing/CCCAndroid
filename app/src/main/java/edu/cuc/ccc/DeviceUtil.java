package edu.cuc.ccc;

import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.X500NameBuilder;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.cert.X509v3CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.UUID;

import edu.cuc.ccc.backends.DeviceManager;

import static edu.cuc.ccc.MyApplication.appContext;

public class DeviceUtil {

    // 表示设备状态的枚举类型
    public enum DeviceStatus {
        // 初始状态
        Unknown,
        // 已配对
        Paired,
        // 正在配对
        Pairing
    }

    public enum AddrType {
        // 无知者无畏
        Unknown,
        LAN,
        WLAN,
        BT,
        // 低功耗蓝牙设备带宽太小，就暂时不考虑了
//        BLE
    }

    public static class IPPortAddr {
        private AddrType type;
        private InetAddress addr;
        private int port;

        public IPPortAddr(AddrType type, InetAddress addr, int port) {
            this.type = type;
            this.addr = addr;
            this.port = port;
        }

        public IPPortAddr(InetAddress addr, int port) {
            this.type = AddrType.Unknown;
            this.addr = addr;
            this.port = port;
        }

        public InetAddress getAddr() {
            return addr;
        }

        public int getPort() {
            return port;
        }

        public AddrType getType() {
            return type;
        }
    }

    public enum DeviceType {
        Unknown,
        PC,
        Phone;

        public int getDrawableId() {
            switch (this) {
                case PC:
                    return R.drawable.ic_computer_black_24dp;
                case Phone:
                    return R.drawable.ic_smartphone_black_24dp;
                default:
                    return R.drawable.ic_help_black_24dp;
            }
        }

        public static DeviceType valueOfEX(String name) {
            try {
                return valueOf(DeviceType.class, name);
            } catch (Exception e) {
                return DeviceType.Unknown;
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    // 解析传入的json数据，如解析不成功返回null，只有在扫描二维码时才需要解析json
    public static Device parseJSONStr(final String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            int ver = jsonObject.getInt(appContext.getResources().getString(R.string.KEY_VERSION));
            DeviceType type;
            try {
                type = DeviceType.valueOfEX(jsonObject.getString(appContext.getResources().getString(R.string.KEY_DEVICE_TYPE)));
            } catch (IllegalArgumentException e) {
                type = DeviceType.Unknown;
            }
            String uuid = jsonObject.getString(appContext.getResources().getString(R.string.KEY_DEVICE_UUID));
            String name = jsonObject.getString(appContext.getResources().getString(R.string.KEY_DEVICE_NAME));
            String pin = jsonObject.getString(appContext.getResources().getString(R.string.KEY_PIN));
            Device device = new Device(uuid, name, type, pin);
            device.setProtocolVersion(ver);
            return device;
        } catch (Exception e) {
            return null;
        }
    }

    //----------------------------------------------------------------------------------------------

    public static void setMyName(String name) {
        if (name != null) {
            SharedPreferences applicationSharedPreferences = MySharedPreferences.getApplicationSharedPreferences();
            applicationSharedPreferences.edit().putString(appContext.getString(R.string.KEY_DEVICE_NAME), name).apply();
        }
        if (DeviceManager.getInstance() != null && DeviceManager.getInstance().getMyDevice() != null) {
            DeviceManager.getInstance().getMyDevice().setName(name);
        }
    }

    static void generateMyInfo() throws OperatorCreationException, CertificateException, NoSuchAlgorithmException {
        Device myDevice = DeviceManager.getInstance().getMyDevice();
        SharedPreferences applicationSharedPreferences = MySharedPreferences.getApplicationSharedPreferences();

        // 生成uuid与name（不想获取系统名称，懒）
        String uuid = UUID.randomUUID().toString();

        // 生成密钥对
        KeyPair keyPair;
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.genKeyPair();

        // 生成证书
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, uuid);
        X500Name name = nameBuilder.build();
        Date from = new Date();
        Date to = new Date(from.getTime() + 365 * 24 * 60 * 60 * 1000L);
        final BouncyCastleProvider BC = new BouncyCastleProvider();
        X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                name,
                BigInteger.ONE,
                from,
                to,
                name,
                keyPair.getPublic());
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                .setProvider(BC).build(keyPair.getPrivate());
        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider(BC)
                .getCertificate(certificateBuilder.build(contentSigner));

        // 设置变量
        myDevice.setUUID(uuid);
        myDevice.setName(uuid);
        myDevice.setCertificate(certificate);
        myDevice.setPrivateKey(keyPair.getPrivate());
        myDevice.setPublicKey(keyPair.getPublic());
        myDevice.setType(DeviceType.Phone);

        // 存储
        applicationSharedPreferences.edit()
                .putString(appContext.getString(R.string.KEY_DEVICE_UUID), uuid)
                .putString(appContext.getString(R.string.KEY_DEVICE_NAME), uuid)
                .putString(appContext.getString(R.string.KEY_DEVICE_PUBLIC_KEY), getBase64EncodedStringFromPublicKey(keyPair.getPublic()))
                .putString(appContext.getString(R.string.KEY_DEVICE_PRIVATE_KEY), getBase64EncodedStringFromPrivateKey(keyPair.getPrivate()))
                .putString(appContext.getString(R.string.KEY_DEVICE_CERTIFICATE), getBase64EncodedStringFromCertificate(certificate))
                .apply();
    }

    static void loadMyInfo() throws InvalidKeySpecException, NoSuchAlgorithmException, CertificateException {
        Device myDevice = DeviceManager.getInstance().getMyDevice();
        SharedPreferences applicationSharedPreferences = MySharedPreferences.getApplicationSharedPreferences();

        myDevice.setUUID(applicationSharedPreferences.getString(
                appContext.getString(R.string.KEY_DEVICE_UUID), null));
        myDevice.setName(applicationSharedPreferences.getString(
                appContext.getString(R.string.KEY_DEVICE_NAME), null));
        myDevice.setType(DeviceType.valueOfEX(applicationSharedPreferences.getString(
                appContext.getString(R.string.KEY_DEVICE_TYPE), null)));
        myDevice.setPublicKey(getPublicKeyFromBase64EncodedString(applicationSharedPreferences.getString(
                appContext.getString(R.string.KEY_DEVICE_PUBLIC_KEY), null)));
        myDevice.setPrivateKey(getPrivateKeyFromBase64EncodedString(applicationSharedPreferences.getString(
                appContext.getString(R.string.KEY_DEVICE_PRIVATE_KEY), null)));
        myDevice.setCertificate(getX509CertificateFromBase64EncodedString(applicationSharedPreferences.getString(
                appContext.getString(R.string.KEY_DEVICE_CERTIFICATE), null)));

    }

    //----------------------------------------------------------------------------------------------

    public static X509Certificate getX509CertificateFromBase64EncodedString(String str) throws CertificateException {
        return (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(
                        new ByteArrayInputStream(Base64.decode(str, 0)));
    }

    public static PrivateKey getPrivateKeyFromBase64EncodedString(String str) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(str, 0)));
    }

    public static PublicKey getPublicKeyFromBase64EncodedString(String str) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(str, 0)));
    }

    public static String getBase64EncodedStringFromCertificate(Certificate certificate) {
        try {
            return Base64.encodeToString(certificate.getEncoded(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getBase64EncodedStringFromPrivateKey(PrivateKey key) {
        try {
            return Base64.encodeToString(key.getEncoded(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getBase64EncodedStringFromPublicKey(PublicKey key) {
        try {
            return Base64.encodeToString(key.getEncoded(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
