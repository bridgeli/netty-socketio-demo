package cn.bridgeli.common.base.web.context;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bridgeli.common.base.web.filter.AbstractConfigurationFilter;

public class ThreadContext extends AbstractConfigurationFilter {

    private static final Logger logger = LoggerFactory.getLogger(ThreadContext.class);

    private static ThreadLocal<ThreadObject> THREAD_OBJECT = new ThreadLocal<ThreadObject>() {
        @Override
        protected ThreadObject initialValue() {
            throw new RuntimeException(" 程序未初始化，请在web.xml中 配置 cn.bridgeli.common.base.web.context.ThreadContext 过滤以设置线程变量：\n" + "   <filter>\n" + "     <filter-name>ThreadContext</filter-name>\n"
                    + "     <filter-class>cn.bridgeli.common.base.web.context.ThreadContext</filter-class>\n" + " </filter>\n" + "    <filter-mapping>\n"
                    + "     <filter-name>ThreadContext</filter-name>\n" + "     <url-pattern>/*</url-pattern>\n" + "    </filter-mapping>\n");
        }
    };

    /**
     * 从线程对象中获取数据
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getData(String key) {
        return (T) THREAD_OBJECT.get().data.get(key);
    }

    /**
     * 向线程对象中设置数据， 为保证安全，如果该对象本身有值时会抛出错误
     *
     * @param key
     *            关键字
     * @param obj
     *            要设置的对象
     */
    public static void addData(String key, Object obj) {
        Object oldValue = THREAD_OBJECT.get().data.put(key, obj);
        if (oldValue != null) {
            throw new IllegalArgumentException("数据已存在不能重复设置");
        }
    }

    /**
     * 从线程中移除对象
     *
     * @param key
     *            要移除的对象名
     */
    @SuppressWarnings("unchecked")
    public static <T> T removeData(String key) {
        return (T) THREAD_OBJECT.get().data.remove(key);
    }

    /** 得到servlet请求中的 request */
    public static HttpServletRequest request() {
        return THREAD_OBJECT.get().request;
    }

    /** 得到servlet请求中的 response */
    public static HttpServletResponse response() {
        return THREAD_OBJECT.get().response;
    }

    /** 得到servlet请求中的 客户端的ip 地址 */
    public static String clientIp() {
        ThreadObject threadObj = THREAD_OBJECT.get();
        String ip = threadObj.ip;
        if (ip == null) {
            ip = getClientIp(threadObj.request);
            threadObj.ip = ip;
        }
        return ip;
    }

    /** 得到参数对象 */
    public static Parameter parameter() {
        return THREAD_OBJECT.get().parameter;
    }

    @Override
    protected void initInternal(FilterConfig arg0) throws ServletException {
        logger.info("#init threadContent");
    }

    @Override
    public void destroy() {
        logger.info("#destroy threadContent");
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        THREAD_OBJECT.set(new ThreadObject(request, response));
        filterChain.doFilter(request, response);
        THREAD_OBJECT.set(null);
    }

    private static final String CLIENT_IP = "X-Forwarded-For";

    /**
     * 得到clientIp
     *
     * @param request
     * @return
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader(CLIENT_IP);
        if (ip != null) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 设置新的共享对象
     *
     * @param tHREAD_OBJECT
     */
    public static void setThreadObject(ThreadObject threadObject) {
        THREAD_OBJECT.set(threadObject);
    }

}

