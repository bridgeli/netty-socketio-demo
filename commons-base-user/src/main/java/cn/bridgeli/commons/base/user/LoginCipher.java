package cn.bridgeli.commons.base.user;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.security.Key;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bridgeli.commons.base.user.util.ResouceUtil;

/**
 * 登录串解密对象
 */
public class LoginCipher {
    static Logger logger = LoggerFactory.getLogger(LoginCipher.class);

    static final String ENCRYPT_TYPE = "RSA";
    /** RAS 可加密的最大长度 */
    public static final int RSA_MAX_LENGTH = 117;

    static final Key PUBLIC_KEY = loadKey("public.key");
    static final Key PRIVATE_KEY = loadKey("private.key");

    /** 加载key */
    public static Key loadKey(String name) {
        try {
            URL url = ResouceUtil.findResource(name, LoginCipher.class);
            InputStream in = url.openStream();
            ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(in));

            try {
                return (Key) oin.readObject();
            } finally {
                oin.close();
            }
        } catch (Exception e) {
            logger.error("#login_cipher key " + name + " load error! " + e, e);
            return null;
        }
    }

    public static byte[] decrypt(byte[] bs) throws Exception {
        final Key publicKey = PUBLIC_KEY;
        return decrypt(bs, publicKey);
    }

    public static byte[] decrypt(byte[] bs, Key key) throws Exception {
        assertKeyNotNull(key);
        Cipher cipher = Cipher.getInstance(ENCRYPT_TYPE);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cycleDoFinal(bs, cipher, 128, 128);
    }

    /**
     * 将要处理的数据 固定长度分片处理
     * 
     * @param bs
     * @param cipher
     * @param splitLength
     * @return
     */
    public static byte[] cycleDoFinal(byte[] bs, Cipher cipher, int inSplitLength, int outSplitLength) throws Exception {
        byte[] out = new byte[bs.length / inSplitLength * outSplitLength];
        for (int inOffset = 0, outOffset = 0; bs.length > inOffset;) {
            outOffset += cipher.doFinal(bs, inOffset, inSplitLength, out, outOffset);
            inOffset += inSplitLength;
        }
        return out;
    }

    public static byte[] encrypt(byte[] bs) throws Exception {
        final Key privateKey = PRIVATE_KEY;
        return encrypt(bs, privateKey);
    }

    public static byte[] encrypt(byte[] bs, Key key) throws Exception {
        assertKeyNotNull(key);
        Cipher cipher = Cipher.getInstance(ENCRYPT_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cycleDoFinal(bs, cipher, RSA_MAX_LENGTH, 128);
    }

    /**
     * 断言 key不为空
     */
    private static void assertKeyNotNull(Key key) {
        if (key == null) {
            throw new IllegalStateException("#login_cipher key == null");
        }
    }

}
