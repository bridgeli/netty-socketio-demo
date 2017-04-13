package cn.bridgeli.msg.push.impl.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;
import org.redisson.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.bridgeli.msg.push.entity.Message;
import cn.bridgeli.msg.push.impl.entity.PushUser;
import cn.bridgeli.msg.push.impl.store.RedissonStoreFactory;
import cn.bridgeli.msg.push.impl.util.ConfigUtils;
import cn.bridgeli.msg.push.impl.util.PushUtils;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.corundumstudio.socketio.listener.ExceptionListener;
import com.corundumstudio.socketio.namespace.Namespace;
import com.corundumstudio.socketio.protocol.Packet;
import com.corundumstudio.socketio.protocol.PacketType;
import com.corundumstudio.socketio.store.StoreFactory;
import com.corundumstudio.socketio.store.pubsub.DispatchMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubStore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PushServer {

    Logger logger = LoggerFactory.getLogger(PushServer.class);

    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public static final PushServer pushServer = new PushServer();
    private Namespace pushNamespace;
    private SocketIOServer server;

    private String origin = ConfigUtils.get("server.origin");
    private String context = ConfigUtils.get("server.context");
    private int port = ConfigUtils.getInt("server.port", -1);
    private String redisAddresses = ConfigUtils.get("redis.addresses");
    private int redisDB = ConfigUtils.getInt("redis.db", 3);

    private PushServer() {
        final Configuration configuration = new Configuration();
        StoreFactory redissonStoreFactory = getRedissonStoreFactory();
        configuration.setStoreFactory(redissonStoreFactory); // redisson
        configuration.setAuthorizationListener(new CookieUserAuthorizationListener()); // auth
        configuration.setPort(port); // port
        configuration.setContext(context); // context
        configuration.setOrigin(origin);

        server = new SocketIOServer(configuration);
        pushNamespace = (Namespace) server.addNamespace(context);

        // exception listener
        configuration.setExceptionListener(new ExceptionListener() {
            @Override
            public void onEventException(Exception e, List<Object> args, SocketIOClient client) {
                e.printStackTrace();
            }

            @Override
            public void onDisconnectException(Exception e, SocketIOClient client) {
                e.printStackTrace();
            }

            @Override
            public void onConnectException(Exception e, SocketIOClient client) {
                e.printStackTrace();
            }

            @Override
            public boolean exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
                e.printStackTrace();
                return false;
            }
        });

        // business handler
        pushNamespace.addEventListener("send-message", Map.class, new DataListener<Map>() {
            @Override
            public void onData(SocketIOClient client, Map data, AckRequest ackSender) throws Exception {
                PushUser user = CookieUserAuthorizationListener.getUser(client.getHandshakeData());
                String toUserId = (String) data.get("userId");
                String content = (String) data.get("content");
                String companyId = (String) data.get("companyId");
                String type = (String) data.get("type");
                Message message = new Message(type, content, toUserId, companyId);
                logger.info("from: " + user.getUserId() + " userId: " + toUserId + " companyId: " + companyId + " type: " + type + " content: " + content);
                push(message);
            }
        });

        pushNamespace.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                UUID sid = client.getSessionId();
                PushUser user = CookieUserAuthorizationListener.getUser(client.getHandshakeData());
                logger.info("disconnect userId: " + user.getUserId() + " companyId: " + user.getCompanyId() + " sid: " + sid);
            }
        });

        pushNamespace.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {

                UUID sid = client.getSessionId();
                PushUser user = CookieUserAuthorizationListener.getUser(client.getHandshakeData());
                if (user != null) {
                    Set<String> rooms = client.getAllRooms();
                    for (String room : rooms) {
                        client.leaveRoom(room);
                    }
                    String userId = Integer.toString(user.getUserId());
                    String companyId = Integer.toString(user.getCompanyId());
                    String companyIdUserId = PushUtils.getCompanyIdUserId(userId, companyId);

                    client.joinRoom(userId); // userId
                    logger.info("userId: " + userId + " companyId: " + companyId + " sid: " + sid + " join room :" + userId);

                    client.joinRoom(companyIdUserId); // companyIdUserIds
                    logger.info("userId: " + userId + " companyId: " + companyId + " sid: " + sid + " join room :" + companyIdUserId);

                } else {
                    client.disconnect();
                    logger.warn("sid: {} has no userId", sid);
                }
            }
        });
    }

    public RedissonStoreFactory getRedissonStoreFactory() {
        // TODO 这里现在使用的Redis单点
        String[] addresses = redisAddresses.split(",");
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setAddress(addresses[0]);
        singleServerConfig.setDatabase(redisDB);
        singleServerConfig.setTimeout(3000);
        singleServerConfig.setConnectionPoolSize(50);
        RedissonClient redissonClient = Redisson.create(config);
        return new RedissonStoreFactory(redissonClient);
    }

    /**
     * 推送消息
     * 
     * @param message
     */
    public void push(Message message) {
        // log 信息
        String content = message.getContent();
        String type = message.getType();
        String userId = message.getUserId();
        String companyId = message.getCompanyId();
        String json = gson.toJson(message);

        if (userId == null) {
            throw new NullPointerException("userId 不能为空");
        }
        String room = null;
        if (companyId == null) {
            room = userId;
        } else {
            room = PushUtils.getCompanyIdUserId(userId, companyId);
        }

        logger.info("send message to: " + room + ", type: " + type + ", content: " + content + ", push: " + json);
        // 获取 redis
        final PubSubStore pubSubStore = server.getConfiguration().getStoreFactory().pubSubStore();

        // 组装消息
        Packet packet = new Packet(PacketType.MESSAGE);
        packet.setSubType(PacketType.EVENT);
        packet.setName("message");
        ArrayList<Object> data = new ArrayList<Object>();
        data.add(json);
        packet.setData(data);
        packet.setNsp(pushNamespace.getName());

        // 当前服务推送
        try {
            Iterable<SocketIOClient> clients = pushNamespace.getRoomClients(room);
            for (SocketIOClient socketIOClient : clients) {

                socketIOClient.send(packet);
            }
        } catch (Exception e) {
            logger.error("当前服务直接推送失败", e);
        }

        // 分发消息(当前服务不会向client推送自己分发出去的消息)
        try {
            pubSubStore.publish(PubSubStore.DISPATCH, new DispatchMessage(userId, packet, pushNamespace.getName()));
        } catch (Exception e) {
            logger.error("分发消息失败", e);
        }
    }

    /**
     * 启动服务
     */
    public void start() {
        Future<Void> future = server.startAsync();
        try {
            future.get();
        } catch (Exception e) {
            logger.error("push server start failed", e);
            System.exit(-1);
        }
    }

    /**
     * 停止服务
     */
    public void stop() {
        server.stop();
    }

}
