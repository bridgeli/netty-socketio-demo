package cn.bridgeli.msg.push.impl.util;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtils {

    private static Properties properties = new Properties();
    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    static {
        try {
            properties.load(ConfigUtils.class.getClassLoader().getResourceAsStream(
                    "config.properties"));
        } catch (Exception e) {
            System.out.println("未找到配置文件 config.properties");
            logger.error("未找到配置文件 config.properties");
            e.printStackTrace();
        }
    }

    public static int getInt(String key, int defaultValue) {
        try {
            String str = ConfigUtils.get(key);
            if (str != null) {
                str = str.trim();
                int value = Integer.parseInt(str);
                return value;
            }
        } catch (Exception e) {
            logger.error("读取配置 {} 时发生异常", key);
            e.printStackTrace();
        }
        return defaultValue;
    }

    public static String get(String key) {
        String value = properties.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        if (value == null) {
            logger.warn("config[{}] not found!", key);
        }
        return value;
    }
}
