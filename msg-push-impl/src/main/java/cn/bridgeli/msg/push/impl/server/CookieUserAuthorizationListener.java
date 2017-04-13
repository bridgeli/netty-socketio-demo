package cn.bridgeli.msg.push.impl.server;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bridgeli.commons.base.user.SimpleUser;
import cn.bridgeli.commons.base.user.Token;
import cn.bridgeli.commons.base.user.TokenManager;
import cn.bridgeli.msg.push.impl.entity.PushUser;

import com.corundumstudio.socketio.AuthorizationListener;
import com.corundumstudio.socketio.HandshakeData;

public class CookieUserAuthorizationListener implements AuthorizationListener {

    Logger logger = LoggerFactory.getLogger(CookieUserAuthorizationListener.class);

    @Override
    public boolean isAuthorized(HandshakeData data) {
        PushUser user = CookieUserAuthorizationListener.getUser(data);
        if (user == null) {
            logger.info("auth failed. userId: {} ");
            return false;
        } else {
            logger.info("auth success userId: {} companyId: ", user.getUserId(), user.getCompanyId());
            return true;
        }
    }

    /**
     * 从Http Cookie中获取用户Id
     * 
     * @param data
     * @return
     */
    public static final PushUser getUser(HandshakeData data) {
        String _cookie = data.getSingleHeader(HttpHeaders.Names.COOKIE);
        if (_cookie != null) {
            Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(_cookie);
            for (Cookie cookie : cookies) {
                if (TokenManager.LOGIN_COOKIE_NAME.equals(cookie.name())) {
                    String value = cookie.value();
                    if (value != null) {
                        return getUserIdFromCookie(value);
                    }
                }
            }
        }
        return null;
    }

    /**
     * 从 cookie 中解析用户身份, 包括用户Id 和 公司Id
     * 
     * @param cookie
     * @return
     */
    private static PushUser getUserIdFromCookie(String cookie) {
        int userId = -1;
        int companyId = -1;
        try {
            Token token = TokenManager.getInstance().getToken(cookie);
            if (token != null) {
                SimpleUser simpleUser = token.getSimpleUser();
                if (simpleUser != null) {
                    userId = simpleUser.getUserId();
                    companyId = simpleUser.getCurrentCompanyId();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (userId <= 0 || companyId <= 0) {
            return null;
        }
        return new PushUser(userId, companyId);
    }
}
