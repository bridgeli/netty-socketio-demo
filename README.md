# netty-socketio-demo

登陆成功之后，当前用户为：userInfo，需要写cookie

```java
try {
    SimpleUser simpleUser = new SimpleUser.Builder().userId(userInfo.getId()).currentCompanyId(userInfo.getCompanyId()).build();
    SessionManager.writeSessionUser(simpleUser);
    TokenManager.getInstance().writeSessionLogin(simpleUser);
    LOG.info("用户 {} 设置登录状态成功", new Object[] { simpleUser });
} catch (Exception e) {
    e.printStackTrace();
    LOG.error("用户 {} 设置登录状态失败 ： {}", new Object[] { e });
}
```

nginx的配置：

    upstream bridgeli_push{

      server  127.0.0.1:11320;
      #check interval=3000 rise=2 fall=3 timeout=40000 type=http;
      #check_http_send "HEAD /check HTTP/1.0\r\n\r\n";
      #check_http_expect_alive http_2xx http_3xx;

    }

    server {
      listen                        80;
      server_name    www.bridgeli.cn;

      location /push {
        proxy_pass http://bridgeli_push;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_send_timeout 14400;
        proxy_read_timeout 14400;
 
      }

    }
