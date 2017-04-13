package cn.bridgeli.commons.base.user;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户令牌
 */
public class Token {
    static Logger logger = LoggerFactory.getLogger(Token.class);

    /** 匿名令牌 */
    public static final Token ANONYMOUS_TOKEN = new Token.Builder()
    .createDate(0)
    .simpleUser(SimpleUser.ANONYMOUS_USER)
    .build();

    /**
     * 创建一个token对象
     * @param serverName 生成令牌的服务器名
     * @param ip 生成令牌时客户端的IP
     * @param su 相关的简单用户信息
     * @return
     */
    public static Token newInstance (String serverName, String ip, SimpleUser simpleUser) {
        return new Token.Builder()
        .serverName(serverName)
        .ip(ip)
        .simpleUser(simpleUser)
        .build();
    }

    /** 校验串默认有效期 */
    public static final long DEFAULT_VAILD_TERM = 7L * 24 * 60 * 60 * 1000;
    /** 校验串默认域名匹配 */
    private static final String DEFAULT_ALLOW_SERVER_NAME_REGEX = ".*";
    /** 校验串默认IP匹配 */
    private static final String DEFAULT_ALLOW_IP_REGEX = ".*";

    /**
     * 判断令牌对象是否有效（默认条件）
     */
    public boolean isValid() {
        return isValid(DEFAULT_VAILD_TERM, DEFAULT_ALLOW_SERVER_NAME_REGEX, DEFAULT_ALLOW_IP_REGEX);
    }

    /**
     * 判断令牌对象是否有效
     * @param timeout 超时时间
     * @param allowServerName 允许的服务器名
     * @param allowIp 允许的ip
     */
    public boolean isValid(long timeout, String allowServerName, String allowIp) {
        if (this == ANONYMOUS_TOKEN) {
            return false;
        }
        if (createDate + timeout < System.currentTimeMillis()) {
            logger.info("#token_fiale createDate " + new SimpleDateFormat("yyyy-MM-dd hh-mm-ss") + " 太早，已失效");
            return false;
        }
        if (!serverName.matches(allowServerName)) {
            logger.info("#token_fiale_serverName 服务器名错误 " + serverName);
            return false;
        }
        if (!ip.matches(allowIp)) {
            logger.info("#token_fiale_ip 客户端ip错误" + ip);
            return false;
        }
        return true;
    }

    /**
     * 获取有效的用户对象
     */
    public SimpleUser getSimpleUser() {
        return isValid() ? simpleUser : SimpleUser.ANONYMOUS_USER;
    }

    /** 将权限对象编码成字节流  暂不提供外部访问，需要时再打开  */
    static byte[] encode(Token token) throws Exception {
        return token.encode();
    }

    /** 将权限对象编码成字节流  暂不提供外部访问，需要时再打开  */
    byte[] encode() throws Exception {
        ByteBufferPlus bf = ByteBufferPlus.allocate(LoginCipher.RSA_MAX_LENGTH * 2);
        bf.put(VERIFY_SIGN);
        bf.putLong(createDate);
        bf.putStringShortLength(serverName);
        bf.putStringShortLength(ip);
        SimpleUser.write(bf, simpleUser);
        return LoginCipher.encrypt(bf.array());
    }

    /** 从字节流解析出一个token 暂不提供外部访问，需要时再打开 */
    static Token decode(ByteBufferPlus bf) {
        byte[] bs = new byte[VERIFY_SIGN.length];
        bf.get(bs);
        if (!Arrays.equals(bs, VERIFY_SIGN)) {
            TokenManager.logger.info("#token_fail verify sign error" );
            return ANONYMOUS_TOKEN;
        }

        Token token = new Builder()
        .createDate(bf.getLong())
        .serverName(bf.getStringByteLength())
        .ip(bf.getStringByteLength())
        .simpleUser(SimpleUser.readSimpleUser(bf))
        .build();

        return token;
    }

    private Token(Builder builder) {
        userId     = builder.userId;
        createDate  = builder.createDate;
        serverName = builder.serverName;
        ip         = builder.ip;
        simpleUser = builder.simpleUser;
    }

    /** 用户id */
    public final int userId;
    /** 令牌创建时间 */
    public final long createDate;
    /** 生成令牌的服务器名 */
    public final String serverName;
    /** 生成令牌时客户端的IP */
    public final String ip;
    /** 相关的简单用户信息 */
    final SimpleUser simpleUser;
    /** 默认校验串 */
    static final byte[] VERIFY_SIGN = "BridgeLiVerifySign".getBytes();

    /**
     * 得到令牌中原始的用户信息 ！注！
     * 该用户对象不一定是有效的用户对象，第三方如果需要调用，应该使用 getValidSimpleUser
     */
    public SimpleUser getRawSimpleUser1() {
        return simpleUser;
    }

    /** 得到用户id */
    public int getUserId() {
        return userId;
    }

    /** 得到令牌创建时间 */
    public long getCreateDate() {
        return createDate;
    }

    /** 得到生成令牌的服务器名 */
    public String getServerName() {
        return serverName;
    }

    /** 得到生成令牌时客户端的IP */
    public String getIp() {
        return ip;
    }

    @Override
    public String toString() {
        return "token " + createDate + " " + serverName + " " + ip + " " + simpleUser;
    }

    /**
     * 令牌构造器
     * @author 徐峰
     * 2011-9-5
     * version 0.01
     */
    public static class Builder {
        /** 用户id */
        private int userId;
        /** 令牌创建时间 */
        private long createDate = System.currentTimeMillis();
        /** 生成令牌的服务器名 */
        private String serverName;
        /** 生成令牌时客户端的IP */
        private String ip;
        /** 相关的简单用户信息 */
        private SimpleUser simpleUser;

        /** 自定义令牌创建时间， 默认为系统时间 */
        public Builder createDate(long crateDate) {
            this.createDate = crateDate;
            return this;
        }

        /** 生成token的服务器名称 */
        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        /** 生成token时用户的ip */
        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        /** 令牌相关的用户信息 */
        public Builder simpleUser(SimpleUser simpleUser) {
            this.simpleUser = simpleUser;
            this.userId = simpleUser.getUserId();
            return this;
        }

        public Token build() {
            return new Token(this);
        }
    }

    /**
     * 断言用户登录，如果没有登录则抛出错误
     */
    public void assertLogin() throws LoginException {
        if (!isValid()) {
            throw new LoginException("用户未登录");
        }
    }
}
