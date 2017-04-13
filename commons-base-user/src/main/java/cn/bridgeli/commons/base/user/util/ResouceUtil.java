package cn.bridgeli.commons.base.user.util;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 获取资源文件工具
 */
public class ResouceUtil {

    private static final Logger logger = LoggerFactory.getLogger(ResouceUtil.class);

    /**
     * 按顺序查找指定名称的文件，返回最先找到的路径，都没找到返回null，具体顺序为：
     * 1.在资源目录下查找
     * 2.在clazz.getResource(name) 中寻找
     * @param name 要寻找的文件名
     */
    public static URL findResource(String name, Class<?> clazz) {
        URL url = null;

        //1.在资源目录下查找
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().getResource(name);
        }

        //2.在clazz.getResource(name) 中寻找
        if (url == null && clazz != null) {
            url = clazz.getResource(name);
        }

        logger.info("读取配置文件：" + name + " 位置 " + url);
        return url;
    }

}
