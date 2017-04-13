package cn.bridgeli.common.base.web.context;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ThreadObject {

    public final HttpServletRequest request;
    public final HttpServletResponse response;
    public String ip;
    /** 其他附加数据 */
    final Map<String, Object> data = new HashMap<String, Object>();
    public Parameter parameter;

    public ThreadObject(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.parameter = null;
        if (request instanceof HttpServletRequest) {
            this.parameter = new Parameter((HttpServletRequest) request);
        }
    }

}