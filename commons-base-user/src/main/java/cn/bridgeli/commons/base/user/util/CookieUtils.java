package cn.bridgeli.commons.base.user.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理cookie的工具
 */
public class CookieUtils {
    static Logger logger = LoggerFactory.getLogger(CookieUtils.class);

    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie cookies[] = request.getCookies();
        if (cookies == null || name == null || name.length() == 0) {
            return null;
        }
        Cookie cookie = null;
        for (int i = 0; i < cookies.length; i++) {
            if (!cookies[i].getName().equals(name)) {
                continue;
            }
            cookie = cookies[i];
            if (request.getServerName().equals(cookie.getDomain())) {
                break;
            }
        }

        return cookie;
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response,
            Cookie cookie, String domain, String path) {
        if (cookie != null) {
            if(StringUtils.isNotBlank(domain)){
                cookie.setDomain(domain);
            }
            cookie.setPath(path);
            cookie.setValue("");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response,
            Cookie cookie) {
        if (cookie != null) {
            String path = request.getContextPath() != null ? request.getContextPath() : "/";
            if ("".equals(path)) {
                path = "/";
            }
            cookie.setPath(path);
            cookie.setValue("");
            response.addCookie(cookie);
        }
    }

    public static void deleteCookie(HttpServletResponse response, Cookie cookie, String domain,
            String path) {
        if (cookie != null) {
            if(StringUtils.isNotBlank(domain)){
                cookie.setDomain(domain);
            }
            cookie.setPath(path);
            cookie.setValue("");
            response.addCookie(cookie);
        }
    }

    public static void setCookie(HttpServletRequest request, HttpServletResponse response,
            String name, String value) {
        setCookie(request, response, name, value, 0x278d00);
    }

    public static void setSaveCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, int maxAge) {
        if (value == null){
            value = StringUtils.EMPTY;
        }
        String path = request.getContextPath() != null ? request.getContextPath() : "/";
        if ("".equals(path)){
            path = "/";
        }
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);
        cookie.setVersion(1);
        addSecureParam(request, cookie);
        response.addCookie(cookie);
    }

    public static void setCookie(HttpServletRequest request, HttpServletResponse response,
            String name, String value, int maxAge) {
        if (value == null) {
            value = "";
        }
        String path = request.getContextPath() != null ? request.getContextPath() : "/";
        if ("".equals(path)) {
            path = "/";
        }
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setVersion(1);
        addSecureParam(request, cookie);
        response.addCookie(cookie);
    }

    public static void setSessionCookie(HttpServletRequest request, HttpServletResponse response, String name, String value, String domain, String path) {
        if (logger.isDebugEnabled()) {
            logger.debug("#login_setCookie " + name + " " + value + " " + path);
        }
        if (value == null){
            value = StringUtils.EMPTY;
        }
        if ("".equals(path)){
            path = "/";
        }
        Cookie cookie = new Cookie(name, value);
        if(StringUtils.isNotBlank(domain)){
            cookie.setDomain(domain);
        }
        cookie.setPath(path);
        addSecureParam(request, cookie);
        response.addCookie(cookie);
    }

    public static void setSessionCookie(HttpServletRequest request, HttpServletResponse response, String name, String value) {
        if (value == null){
            value = StringUtils.EMPTY;
        }
        String path = request.getContextPath() != null ? request.getContextPath() : "/";
        if ("".equals(path)){
            path = "/";
        }
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setVersion(1);
        addSecureParam(request, cookie);
        response.addCookie(cookie);
    }

    /**
     * setCookie 根据domain设置作用域和有效期
     * @param response
     * @param name
     * @param value
     * @param domain
     * @param path
     * @param maxAge
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String name, String value,
            String domain, String path, int maxAge){
        if (logger.isDebugEnabled()) {
            logger.debug("#login_setCookie " + name + " " + value + " " + path + " " + maxAge);
        }
        if (value == null) {
            value = "";
        }
        Cookie cookie = new Cookie(name, value);
        if(StringUtils.isNotBlank(domain)){
            cookie.setDomain(domain);
        }
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        addSecureParam(request, cookie);
        response.addCookie(cookie);
    }

    private static void addSecureParam(HttpServletRequest request, Cookie cookie){
        if(cookie == null){
            return;
        }
        String schema = getScheme(request);
        if(schema != null && schema.toLowerCase().startsWith("https")){
            logger.debug("检测到是https请求，设置安全Cookie");
            cookie.setSecure(true);
        }
    }

    private static final String KEY_X_FORWARDED_SCHEME = "X-Forwarded-Scheme";

    private static String getScheme(HttpServletRequest request){
        String forwardScheme = request.getHeader(KEY_X_FORWARDED_SCHEME);
        logger.debug("forwardSchema : {}", forwardScheme);
        if(StringUtils.isNotBlank(forwardScheme)){
            return forwardScheme;
        }
        String schema = request.getScheme();
        if(StringUtils.isNotBlank(schema)){
            return schema;
        }
        return "http";
    }

}
