package cn.bridgeli.commons.base.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bridgeli.common.base.web.context.ThreadContext;
import cn.bridgeli.commons.base.user.util.StringUtil;

import com.google.gson.Gson;

/**
 * session管理类
 */
public class SessionManager {
    static Logger logger = LoggerFactory.getLogger(SessionManager.class);

    public static final String THREAD_CONTENT_LOGIN_USER_KEY = "MDS_LOGIN_USER_KEY";

    public static final String LOGIN_USER_KEY = "MDS_LOGIN_USER_KEY";

    private static Gson gson = new Gson();

    private final static SessionManager tokenManager = new SessionManager();

    public static SessionManager getInstance() {
        return tokenManager;
    }

    /**
     * 得到当前登录的有效用户 1、先查看线程副本 2、如果副本里没有，则从session中获取
     * 
     * @return
     */
    public static SimpleUser currentSessionUser() {
        SimpleUser simpleUser = ThreadContext.getData(THREAD_CONTENT_LOGIN_USER_KEY);

        if (null == simpleUser) {
            HttpServletRequest request = ThreadContext.request();
            if (request != null) {
                HttpSession session = request.getSession();
                Object obj = session.getAttribute(LOGIN_USER_KEY);
                if (null != obj) {
                    String userStr = (String) session.getAttribute(LOGIN_USER_KEY);
                    simpleUser = decode(userStr);
                    ThreadContext.addData(THREAD_CONTENT_LOGIN_USER_KEY, simpleUser);
                }
            }
        }
        return simpleUser;
    }

    /**
     * 向sessione中写入用户登录信息
     * 
     * @param simpleUser
     */
    public static SimpleUser writeSessionUser(SimpleUser simpleUser) {
        HttpServletRequest request = ThreadContext.request();
        if (null != request) {
            HttpSession session = request.getSession();
            session.setAttribute(LOGIN_USER_KEY, gson.toJson(simpleUser));
        }
        return simpleUser;
    }

    /**
     * 清除用户登录信息
     * 
     * @param request
     */
    public static void clearSessionUser() {
        HttpServletRequest request = ThreadContext.request();
        if (null != request) {
            HttpSession session = request.getSession();
            session.removeAttribute(LOGIN_USER_KEY);
        }

    }

    private static String encode(SimpleUser simpleUser) {
        String userStr = null;
        if (null != simpleUser) {
            userStr = gson.toJson(simpleUser);
        }
        return gson.toJson(simpleUser);
    }

    private static SimpleUser decode(String userStr) {

        SimpleUser simpleUser = null;
        if (StringUtil.isNotBlank(userStr)) {
            try {
                simpleUser = gson.fromJson(userStr, SimpleUser.class);
            } catch (Exception e) {
                logger.error("[sessionManager]逆序列化simpleUser异常", e);
                return null;
            }
        }
        return simpleUser;

    }

}
