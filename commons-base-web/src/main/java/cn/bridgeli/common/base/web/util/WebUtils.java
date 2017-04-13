package cn.bridgeli.common.base.web.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebUtils {

    private static Logger logger = LoggerFactory.getLogger(WebUtils.class);

    private static final String XML_HTTP_REQUEST = "XMLHttpRequest";

    private static final String X_REQUESTED_WITH = "X-Requested-With";

    private static final String JSON_CALLBACK = "jsoncallback";

    private static final String SEPARATOR_SLASH = "/";

    /**
     * 是否是跨域请求
     * 
     * @param request
     * @return
     */
    public static boolean isCrossDomain(final HttpServletRequest request) {
        String jsoncallback = request.getParameter(JSON_CALLBACK);
        return jsoncallback != null && jsoncallback.trim().length() > 0;
    }

    /**
     * 得到回调方法名
     * 
     * @param request
     * @return
     */
    public static String getJsonCallbackFunctionName(final HttpServletRequest request) {
        String jsoncallback = request.getParameter(JSON_CALLBACK);
        return jsoncallback == null ? null : jsoncallback.trim();
    }

    /**
     * 判断是否为ajax请求
     * 
     * @param request
     * @return
     */
    public static boolean isAjaxRequest(final HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            Enumeration enums = request.getHeaderNames();
            while (enums.hasMoreElements()) {
                String key = (String) enums.nextElement();
                logger.debug(" 请求头信息： {} 的值对应为： {}", new Object[] { key, request.getHeader(key) });
            }
        }

        boolean isAjaxMatch = XML_HTTP_REQUEST.equalsIgnoreCase(request.getHeader(X_REQUESTED_WITH));

        boolean isCrossDomain = isCrossDomain(request);

        logger.debug("是否是原生ajax请求 ： {} , 是否是跨域请求 : {}, 综合： {}", new Object[] { isAjaxMatch, isCrossDomain, isAjaxMatch || isCrossDomain });

        return isAjaxMatch || isCrossDomain;
    }

    private static final String UTF_8 = "UTF-8";

    /**
     * 输出内容
     * 
     * @param response
     * @param content
     * @throws IOException
     */
    public static void wirteResponse(HttpServletResponse response, String content) throws IOException {
        PrintWriter out = response.getWriter();
        response.setCharacterEncoding(UTF_8); // 先指定输出流的编码
        out = response.getWriter();
        out.print(content);
        out.flush();
        out.close();
    }

    private static final String WEIXIN_REQUEST_MARK = "micromessenger";
    protected static final String USER_AGENT = "user-agent";

    /**
     * 是否是微信内置浏览器
     * 
     * @param request
     * @return
     */
    public static boolean isWeixinRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String agent = request.getHeader(USER_AGENT);
        return agent != null && agent.toLowerCase().contains(WEIXIN_REQUEST_MARK);
    }

    /**
     * 得到项目根路径(形如: http://bridgeli.cn/)
     * 
     * @param request
     * @return
     */
    public static String getWebRoot(HttpServletRequest request) {
        // 得到协议如：http
        String scheme = request.getScheme();
        // 得到服务器名称如：127.0.0.1
        String serverName = request.getServerName();
        // 得到端口号如8080
        int serverPort = request.getServerPort();
        // 得到当前上下文路径，也就是安装后的文件夹位置。
        String contextPath = request.getContextPath();
        // 连起来拼成完整的url
        StringBuffer sb = new StringBuffer();
        sb.append(scheme);
        sb.append("://");
        sb.append(serverName);
        if (80 != serverPort) {
            sb.append(":");
            sb.append(serverPort);
        }
        sb.append(contextPath);
        sb.append("/");
        return sb.toString();
    }

    /**
     * 得到请求的url
     * 
     * @param request
     * @return
     */
    public static String getRealUrl(HttpServletRequest request) {
        StringBuffer realUrl = new StringBuffer();
        realUrl.append(request.getScheme());
        realUrl.append("://").append(request.getHeader("host"));
        realUrl.append(request.getRequestURI());
        if (request.getQueryString() != null) {
            realUrl.append("?").append(request.getQueryString());
        }
        return realUrl.toString();
    }

    private static final String DOUBLE_SPRIT = "//";

    /**
     * 得到不包含项目名的链接
     * 
     * @param request
     * @return
     */
    public static String getShortURI(HttpServletRequest request) {
        String shortUrl = StringUtils.replaceOnce(request.getRequestURI(), request.getContextPath(), StringUtils.EMPTY);
        // String shortUrl = request.getRequestURI().replaceFirst(request.getContextPath(), StringUtils.EMPTY);
        shortUrl = StringUtils.EMPTY.equals(shortUrl) ? SEPARATOR_SLASH : shortUrl;

        while (shortUrl.startsWith(DOUBLE_SPRIT)) {
            shortUrl = shortUrl.substring(1);
        }
        return shortUrl;
    }

    /**
     * ?
     */
    public static final String PARAM_SEPARATOR_QUESTION_MARK = "?";

    /**
     * &
     */
    public static final String PARAM_SEPARATOR_AND = "&";

    /**
     * &
     */
    public static final String PARAM_SEPARATOR_EQUAL = "=";

    /**
     * 向url添加新参数
     * 
     * @param targetUrl
     *            目标url
     * @param key
     * @param value
     * @return
     */
    public static String addParamToUrl(String targetUrl, String key, String value) {
        if (StringUtils.isBlank(targetUrl)) {
            return StringUtils.EMPTY;
        }
        StringBuffer sb = new StringBuffer();
        int index = targetUrl.indexOf(PARAM_SEPARATOR_QUESTION_MARK);
        if (index == -1) {// 没有问号
            sb.append(targetUrl);
            sb.append(PARAM_SEPARATOR_QUESTION_MARK);
            sb.append(key);
            sb.append(PARAM_SEPARATOR_EQUAL);
            sb.append(value);
        } else {
            sb.append(targetUrl.substring(0, index + 1));
            sb.append(key);
            sb.append(PARAM_SEPARATOR_EQUAL);
            sb.append(value);
            if (index != (targetUrl.length() - 1)) {// 问号不在最末尾
                sb.append(PARAM_SEPARATOR_AND);
                sb.append(targetUrl.substring(index + 1));
            }
        }
        return sb.toString();
    }

    /**
     * 获取客户端真实ip
     *
     * @param request
     * @return
     */
    public static String getClientRealIp(HttpServletRequest request) {
        if (null == request) {
            return null;
        }
        return (request.getHeader("X-Real-IP") == null || request.getHeader("X-Real-IP").length() == 0) ? request.getRemoteAddr() : request.getHeader("X-Real-IP");
    }
}
