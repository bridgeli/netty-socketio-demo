package cn.bridgeli.commons.base.user.util;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 字符串相关工具
 */
public class StringUtil {
    static Logger log = LoggerFactory.getLogger(StringUtil.class);

    // 主域名正则
    private static final Pattern MAIN_DOMAIN = Pattern.compile(
            "(?<=http://|\\.)[^.]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE);

    /** 将输入对象转换成String，输入对象为null时 返回空字符串 "" */
    public static String toStringNoNull(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    /**
     * 返回一个对象字符串是否为 null 或者 ""
     * @param obj 要判断的对象
     * @return 是否为null 或者 ""
     */
    public static boolean isEmpty(Object obj) {
        return obj == null || obj.toString().isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 返回拆分后的最后一个字符
     * @param str
     * @param regex
     * @return
     */
    public static String splitLast(String str, String regex) {
        String[] split = str.split(regex);
        return split[split.length - 1];
    }

    /**
     * 取得拆分后的最后一个字符串 substringAfterLast("foo.bar.baz", ".") == "baz"
     * @param str 要拆分的字符串
     * @param split 拆分字符
     * @return 最后一个字符串
     */
    public static String substringAfterLast(String str, String split) {
        String[] strs = str.split(split);
        if (strs.length == 0) {
            return "";
        }
        return strs[strs.length - 1];
    }

    /**
     * 连接字符串数组，将其连成一个字符串。
     * @param str	要连接的字符串数组
     * @return		连接后的字符串
     */
    public static String connect(Object[] str) {
        return connect(str, "\n");
    }

    /**
     * 连接字符串数组，将其连成一个字符串。
     * @param str	要连接的字符串数组
     * @param separator 两个字符串之间的分割符号
     * @return		连接后的字符串
     */
    public static String connect(Object[] str, String separator) {
        if (str == null) {
            return null;
        }
        if (str.length == 0) {
            return "";
        }

        StringBuffer result = new StringBuffer("");
        for (int i = 0; i < str.length - 1; i++) {
            result.append(str[i]);
            result.append(separator);
        }
        result.append(str[str.length - 1]);

        return result.toString();
    }

    /**
     * 取主域名
     * @param url
     * @return
     */
    public static String getMainDomain(String url) {
        Matcher matcher = MAIN_DOMAIN.matcher(url);
        if(matcher.find()){
            return matcher.group();
        };
        return null;
    }

    public static String getMD5OfStr(String str) {
        return DigestUtils.md5Hex(str);
    }

    /**
     * 将 hex 表示的字符串转换为  byte 数组
     * @param hexString
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
        byte[] result = new byte[hexString.length() / 2];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) ((hexToByte(hexString.charAt(i * 2)) << 4) + hexToByte(hexString.charAt(i * 2 + 1)));
        }
        return result;
    }

    private static int hexToByte(char ch) {
        if (ch >= '0' && ch <= '9'){
            return ch - '0';
        }
        if (ch >= 'a' && ch <= 'f' ) {
            return ch - 'a' + 10;
        }
        if (ch >= 'A' && ch <= 'F') {
            return ch - 'A' + 10;
        }
        throw new RuntimeException("hex to byte Error: " + ch);
    }

    public static String bytesToHexString(byte[] bs) {
        StringBuilder buf = new StringBuilder(bs.length * 2);
        for (byte b : bs) {
            if ((b & 0xff) < 16) {
                buf.append("0");
            }
            buf.append(Long.toString(b & 0xff, 16));
        }
        return buf.toString();
    }

    /**
     * 随机数字符串生成
     * @param size
     * @return
     */
    public static String getRandomNumber(int size){
        char[] c ={ '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
        Random random = new Random(); // 初始化随机数产生器
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size; i++){
            sb.append(c[Math.abs(random.nextInt()) % c.length]);
        }
        return sb.toString();
    }

    /**
     * 获取邮箱前缀
     * @param email
     * @return
     */
    public static String getEmailPrefix(String email){
        if(email == null){
            return "";
        }
        int index = email.lastIndexOf("@");
        if(index < 0){
            return "";
        }
        return email.substring(0, index);
    }
}
