package cn.bridgeli.common.base.web.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bridgeli.common.base.web.util.WebUtils;

/**
 * @Description: 所有拦截器的基类
 */
public abstract class AbstractConfigurationFilter implements Filter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean ignoreStaticFile = true;

    private static final String IGNORE_STATIC_FILE = "ignoreStaticFile";

    /** 静态文件后缀名 */
    private Set<String> ignoreSuffix = new HashSet<String>();

    private static final String[] DEFULT_STATIC_FILE_SUFFIX = new String[] { "jpg", "jpeg", "ico", "txt", "doc", "ppt", "xls", "pdf", "gif", "png", "bmp", "css", "js", "swf", "flv", "mp3", "htc" };

    private static final String IGNORE_STATIC_FILE_SUFFIXS = "ignoreStaticFileSuffixs";

    private static final String EXCLUSIONS = "exclusions";

    private List<String> exclusions;

    private static final String SEPARATOR_COMMA = ",";

    private static final String SEPARATOR_SLASH = "/";

    private static final String PATH_REGX_ALL = "/*";

    @Override
    final public void init(FilterConfig filterConfig) throws ServletException {
        this.setIgnoreStaticFile(parseBoolean(getPropertyFromInitParams(filterConfig, IGNORE_STATIC_FILE, Boolean.TRUE.toString())));
        initStaticFileList(filterConfig);
        initExClusionPath(filterConfig);
        initInternal(filterConfig);
    }

    protected void initInternal(FilterConfig filterConfig) throws ServletException {
        // nothing to do
    }

    private void initExClusionPath(FilterConfig filterConfig) {
        this.exclusions = initPauthListSeparateByComma(filterConfig, EXCLUSIONS);
    }

    /**
     * 初始化用逗号分隔的路径参数
     *
     * @param filterConfig
     * @param key
     * @return
     */
    protected List<String> initPauthListSeparateByComma(FilterConfig filterConfig, String key) {
        String exclusionInitParameterValue = filterConfig.getInitParameter(key);
        if (exclusionInitParameterValue == null || StringUtils.isBlank(exclusionInitParameterValue)) {
            return new ArrayList<String>(0);
        }
        if (exclusionInitParameterValue.trim().length() == 0) {
            return new ArrayList<String>(0);
        }

        String[] exclusion = exclusionInitParameterValue.split(SEPARATOR_COMMA);
        for (int i = 0; i < exclusion.length; i++) {
            exclusion[i] = exclusion[i].trim();
            if (!exclusion[i].startsWith(SEPARATOR_SLASH)) {
                exclusion[i] = SEPARATOR_SLASH + exclusion[i];
            }
        }
        return Arrays.asList(exclusion);
    }

    /**
     * 判断路径是否被拦截
     *
     * @param request
     * @return
     */
    protected boolean isExcludedPath(final HttpServletRequest request) {
        return isContainsUrl(request, this.exclusions);
    }

    /**
     * 是否包含在目标的url集合里
     *
     * @param request
     * @param urls
     * @return
     */
    protected boolean isContainsUrl(final HttpServletRequest request, List<String> urls) {
        if (urls == null || urls.size() == 0) {
            return false;
        }
        String shortUrl = WebUtils.getShortURI(request);
        for (String url : urls) {
            if (url.equals(PATH_REGX_ALL)) {
                return true;
            }
            if (url.endsWith(PATH_REGX_ALL)) {
                String s = url.substring(0, url.length() - 2);
                if (shortUrl.startsWith(s)) {
                    return true;
                }
            }
            if (shortUrl.matches(url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从配置或context中获取值
     *
     * @param filterConfig
     * @param propertyName
     * @param defaultValue
     * @return
     */
    public final String getPropertyFromInitParams(final FilterConfig filterConfig, final String propertyName, final String defaultValue) {
        final String value = filterConfig.getInitParameter(propertyName);
        if (StringUtils.isNotBlank(value)) {
            logger.info("Property [{}] loaded from FilterConfig.getInitParameter with value [{}]", propertyName, value);
            return value;
        }
        final String value2 = filterConfig.getServletContext().getInitParameter(propertyName);
        if (StringUtils.isNotBlank(value2)) {
            logger.info("Property [{}] loaded from ServletContext.getInitParameter with value [{}]", propertyName, value2);
            return value2;
        }
        return defaultValue;
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public final void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 如果配置需要忽略静态资源，该url又是静态资源，就忽略
        if (this.isIgnoreStaticFile() && this.isStaticFile(servletRequest)) {
            logger.debug("url : {}  is static file, ignore it ", new Object[] { WebUtils.getShortURI(request) });
            chain.doFilter(request, response);
            return;
        }

        // 如果是被排除的路径，也忽略
        if (isExcludedPath(request)) {
            logger.debug("url : {}  is excludedPath path, ignore it ", new Object[] { WebUtils.getShortURI(request) });
            chain.doFilter(request, response);
            return;
        }

        doFilter(request, response, chain);
    }

    public abstract void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException;

    protected final boolean parseBoolean(final String value) {
        return ((value != null) && value.equalsIgnoreCase("true"));
    }

    public boolean isIgnoreStaticFile() {
        return ignoreStaticFile;
    }

    public void setIgnoreStaticFile(boolean ignoreStaticFile) {
        this.ignoreStaticFile = ignoreStaticFile;
    }

    // 判断是否是静态资源
    protected boolean isStaticFile(ServletRequest request) {
        boolean retValue = false;
        if (request instanceof HttpServletRequest) {
            String uri = ((HttpServletRequest) request).getRequestURI();
            if (uri == null) {
                retValue = true;
            } else if (uri.lastIndexOf(".") > -1) {
                String suffix = uri.substring(uri.lastIndexOf(".") + 1, uri.length()).toLowerCase();
                retValue = ignoreSuffix.contains(suffix);
            }
        }
        return retValue;
    }

    private void initStaticFileList(FilterConfig filterConfig) {
        // 先初始化默认后缀
        ignoreSuffix.addAll(Arrays.asList(DEFULT_STATIC_FILE_SUFFIX));
        String staticFileList = this.getPropertyFromInitParams(filterConfig, IGNORE_STATIC_FILE_SUFFIXS, StringUtils.EMPTY);
        if (StringUtils.isBlank(staticFileList)) {
            return;
        }

        // 如果列表配置不为空，则要清空列表
        ignoreSuffix.clear();

        // 循环并把配置的加入列表
        String[] lists = staticFileList.split(",");
        List<String> ignoreList = new ArrayList<String>();
        if (lists != null && lists.length > 0) {
            for (String s : lists) {
                if (StringUtils.isNotBlank(s)) {
                    ignoreList.add(s.trim());
                }
            }
            if (ignoreList.size() > 0) {
                ignoreSuffix.addAll(ignoreList);
            }
        }
    }

    @Override
    public void destroy() {

    }

}
