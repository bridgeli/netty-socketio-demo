package cn.bridgeli.msg.push.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.bridgeli.msg.push.impl.server.check.HttpCheckServer;

@Component
public class Bootstrap {

    private static Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    static {
        PushServer.pushServer.start();  // 启动失败, 系统会自动关闭
        final HttpCheckServer checkServer = new HttpCheckServer(11221);
        checkServer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PushServer.pushServer.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    checkServer.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }
}