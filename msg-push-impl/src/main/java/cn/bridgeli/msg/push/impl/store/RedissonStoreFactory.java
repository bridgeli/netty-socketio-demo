package cn.bridgeli.msg.push.impl.store;

import com.corundumstudio.socketio.store.Store;
import com.corundumstudio.socketio.store.pubsub.BaseStoreFactory;
import com.corundumstudio.socketio.store.pubsub.PubSubStore;
import org.redisson.Redisson;
import org.redisson.RedissonClient;

import java.util.Map;
import java.util.UUID;

public class RedissonStoreFactory extends BaseStoreFactory {

    private final RedissonClient redisClient;
    private final RedissonClient redisPub;
    private final RedissonClient redisSub;

    private final PubSubStore pubSubStore;

    public RedissonStoreFactory() {
        this(Redisson.create());
    }

    public RedissonStoreFactory(RedissonClient redisson) {
        this.redisClient = redisson;
        this.redisPub = redisson;
        this.redisSub = redisson;

        this.pubSubStore = new RedissonPubSubStore(redisPub, redisSub, getNodeId());
    }

    public RedissonStoreFactory(Redisson redisClient, Redisson redisPub, Redisson redisSub) {
        this.redisClient = redisClient;
        this.redisPub = redisPub;
        this.redisSub = redisSub;

        this.pubSubStore = new RedissonPubSubStore(redisPub, redisSub, getNodeId());
    }

    @Override
    public Store createStore(UUID sessionId) {
        return new RedissonStore(sessionId, redisClient);
    }

    public PubSubStore pubSubStore() {
        return pubSubStore;
    }

    @Override
    public void shutdown() {
        redisClient.shutdown();
        redisPub.shutdown();
        redisSub.shutdown();
    }

    @Override
    public <K, V> Map<K, V> createMap(String name) {
        return redisClient.getMap(name);
    }

}
