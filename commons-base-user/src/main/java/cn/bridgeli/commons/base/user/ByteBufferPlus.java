package cn.bridgeli.commons.base.user;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 流的序列化和反序列化，增强 ByteBuffer对字符串的处理
 */
public class ByteBufferPlus {
    static Logger logger = LoggerFactory.getLogger(ByteBufferPlus.class);

    final private ByteBuffer proxy;

    public static ByteBufferPlus allocate(int capacity) {
        return new ByteBufferPlus(ByteBuffer.allocate(capacity));
    }

    public ByteBufferPlus(ByteBuffer byteBuffer) {
        this.proxy = byteBuffer;
    }

    public static ByteBufferPlus wrap(byte[] bytes) {
        return new ByteBufferPlus(ByteBuffer.wrap(bytes));
    }

    public void get(byte[] bs) {
        proxy.get(bs);
    }

    public long getLong() {
        return proxy.getLong();
    }

    public byte get() {
        return proxy.get();
    }

    public int getInt() {
        return proxy.getInt();
    }

    /**
     * 读取一个字符串，其中前两位是字符串的长度，后面是实际字符串
     * @return
     */
    public String getStringByteLength() {
        short length = proxy.getShort();
        byte[] bytes = new byte[length];
        proxy.get(bytes);
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("", e);
        }
    }

    /**
     * 写入一个字符串，其中前两位是字符串的长度，后面是实际字符串
     * @param str
     */
    public void putStringShortLength(String str) {
        byte[] bytes;
        try {
            bytes = str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("", e);
        }
        proxy.putShort((byte)bytes.length);
        proxy.put(bytes);
    }

    public void put(byte[] bytes) {
        proxy.put(bytes);
    }

    public void putLong(long l) {
        proxy.putLong(l);
    }

    public void put(byte b) {
        proxy.put(b);
    }

    public void putInt(int n) {
        proxy.putInt(n);
    }

    public byte[] array() {
        return proxy.array();
    }

}
