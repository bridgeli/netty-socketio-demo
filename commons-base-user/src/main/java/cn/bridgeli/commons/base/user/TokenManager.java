package cn.bridgeli.commons.base.user;

import javax.crypto.BadPaddingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bridgeli.common.base.web.context.ThreadContext;
import cn.bridgeli.commons.base.user.util.CookieUtils;
import cn.bridgeli.commons.base.user.util.StringUtil;

/**
 * 令牌管理类，提供客户端使用
 */
public class TokenManager {
    static Logger logger = LoggerFactory.getLogger(TokenManager.class);

    /** cookie中验证串的名称 */
    public static final String LOGIN_COOKIE_NAME = "alpha_login_authToken";

    /** 存放在 threadContent.data 中的当前用户 所使用的 key */
    public static final String THREAD_CONTENT_KEY_LOGIN_TOKEN = "alpha_login_user_token";

    private final static TokenManager tokenManager = new TokenManager();

    public static TokenManager getInstance() {
        return tokenManager;
    }

    /** 得到当前用户的token对象 */
    public Token getThreadContentToken() {
        Token token = ThreadContext.getData(THREAD_CONTENT_KEY_LOGIN_TOKEN);
        if (token == null) {
            HttpServletRequest request = ThreadContext.request();
            TokenManager tokenManager = TokenManager.tokenManager;
            token = tokenManager.getToken(tokenManager.getStringToken(request));
            ThreadContext.addData(THREAD_CONTENT_KEY_LOGIN_TOKEN, token);
        }
        return token;
    }

    /**
     * 得到当前登录的token
     * 
     * @return
     */
    public static Token currentToken() {
        return getInstance().getThreadContentToken();
    }

    /** 得到当前登录的有效用户 */
    public static SimpleUser currentSimpleUser() {
        return currentToken().getSimpleUser();
    }

    /** 通过 reqeust 获取有效用户对象 */
    public SimpleUser getSimpleUser(HttpServletRequest request) {
        return getToken(getStringToken(request)).getSimpleUser();
    }

    /** 通过 request 中获取 校验串 */
    public String getStringToken(HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                final String cookieName = LOGIN_COOKIE_NAME;
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return "";
    }

    /** 通过 校验串中获取令牌对象 */
    public Token getToken(String stringToken) {
        if (!stringToken.isEmpty()) {
            try {
                final String realCryptStr = removeDirectUserInfo(stringToken);
                final String revisedBase64 = reviseBase64(realCryptStr);
                byte[] bs = Base64.decodeBase64(revisedBase64.getBytes());
                final byte[] decrypt = LoginCipher.decrypt(bs);
                ByteBufferPlus bf = ByteBufferPlus.wrap(decrypt);
                return Token.decode(bf);
            } catch (BadPaddingException e) {
                logger.error("#token_decrypt_error " + stringToken + " " + e);
            } catch (Exception e) {
                logger.error("#token_decrypt_error " + e, e);
            }
        }
        return Token.ANONYMOUS_TOKEN;
    }

    /** 通过 令牌中获取默认标准的有效用户 */
    public SimpleUser getSimpleUser(Token token) {
        return token.getSimpleUser();
    }

    /**
     * 通过 用户信息、服务器、客户端ip 获取 令牌对象
     * 
     * @param user
     *            用户信息
     * @param serverName
     *            当前服务器名
     * @param ip
     *            客户端 ip
     * @return
     */
    public Token getToken(SimpleUser user, String serverName, String ip) {
        return Token.newInstance(serverName, ip, user);
    }

    /** 从 令牌对象中 获取 校验串 */
    public String getStringToken(Token token) throws Exception {
        final byte[] encode = Token.encode(token);
        final byte[] base64 = Base64.encodeBase64(encode);
        final String addDirectUserInfo = addDirectUserInfo(new String(base64), token.getSimpleUser());
        return addDirectUserInfo;
    }

    /** 将token对象写入 cookie 中 */
    private void writeCookie(Token token, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("P3P", "CP=\"CURa ADMa DEVa PSAo PSDo OUR BUS UNI PUR INT DEM STA PRE COM NAV OTC NOI DSP COR\"");
        String mainHost = request.getHeader("Host");
        String mainDomain = StringUtil.getMainDomain(mainHost);
        final String simpleUserStrToken = getStringToken(token);
        CookieUtils.setCookie(request, response, TokenManager.LOGIN_COOKIE_NAME, simpleUserStrToken, mainDomain, "/", 30 * 60);
    }

    /** 将token对象写入 cookie 中 */
    private void writeSessionCookie(Token token, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("P3P", "CP=\"CURa ADMa DEVa PSAo PSDo OUR BUS UNI PUR INT DEM STA PRE COM NAV OTC NOI DSP COR\"");
        String mainHost = request.getHeader("Host");
        String mainDomain = StringUtil.getMainDomain(mainHost);
        final String simpleUserStrToken = getStringToken(token);
        CookieUtils.setSessionCookie(request, response, TokenManager.LOGIN_COOKIE_NAME, simpleUserStrToken, mainDomain, "/");
    }

    /**
     * 向cookie中写入用户登录信息
     * 
     * @param receive
     * @param request
     * @param response
     * @throws Exception
     */
    public SimpleUser writeLogin(SimpleUser simpleUser) throws Exception {
        HttpServletRequest request = ThreadContext.request();
        HttpServletResponse response = ThreadContext.response();
        if (request != null && response != null) {
            TokenManager tokenmanager = TokenManager.getInstance();
            Token token = tokenmanager.getToken(simpleUser, request.getHeader("Host"), ThreadContext.clientIp());
            tokenmanager.writeCookie(token, request, response);
        }
        return simpleUser;
    }

    /**
     * 向cookie中写入用户登录信息(session级别的时间)
     * 
     * @param receive
     * @param request
     * @param response
     * @throws Exception
     */
    public SimpleUser writeSessionLogin(SimpleUser simpleUser) throws Exception {
        HttpServletRequest request = ThreadContext.request();
        HttpServletResponse response = ThreadContext.response();
        if (request != null && response != null) {
            TokenManager tokenmanager = TokenManager.getInstance();
            Token token = tokenmanager.getToken(simpleUser, request.getHeader("Host"), ThreadContext.clientIp());
            tokenmanager.writeSessionCookie(token, request, response);
        }
        return simpleUser;
    }

    public void clearTokenCookie(HttpServletRequest request, HttpServletResponse response) {
        String mainHost = request.getHeader("Host");
        String mainDomain = StringUtil.getMainDomain(mainHost);
        CookieUtils.deleteCookie(response, CookieUtils.getCookie(request, TokenManager.LOGIN_COOKIE_NAME), mainDomain, "/");
    }

    /**
     * base64 修正，自动按4的整倍数添加符号：“=” 用来处理 cookie规范1.0 中会截去base64编码最后“=”的问题。
     * 
     * @param realCryptStr
     * @return
     */
    private String reviseBase64(final String realCryptStr) {
        switch (realCryptStr.length() % 4) {
        case 1:
            return realCryptStr + "===";
        case 2:
            return realCryptStr + "==";
        case 3:
            return realCryptStr + "=";
        default:
            return realCryptStr;
        }
    }

    /** 将输入的校验串添加明文的用户信息 */
    private static String addDirectUserInfo(String str, SimpleUser su) {
        return str;
    }

    /** 在输入串中移除附加明文用户信息 */
    private static String removeDirectUserInfo(String str) {
        return str.split("\\|")[0];
    }

}
