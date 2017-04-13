package cn.bridgeli.msg.push.impl.store;

import com.corundumstudio.socketio.store.pubsub.PubSubListener;
import com.corundumstudio.socketio.store.pubsub.PubSubMessage;
import com.corundumstudio.socketio.store.pubsub.PubSubStore;
import io.netty.util.internal.PlatformDependent;
import org.redisson.RedissonClient;
import org.redisson.core.MessageListener;
import org.redisson.core.RTopic;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class RedissonPubSubStore implements PubSubStore {

    private final RedissonClient redissonPub;
    private final RedissonClient redissonSub;
    private final Long nodeId;

    private final ConcurrentMap<String, Queue<Integer>> map = PlatformDependent.newConcurrentHashMap();

    public RedissonPubSubStore(RedissonClient redissonPub, RedissonClient redissonSub, Long nodeId) {
        this.redissonPub = redissonPub;
        this.redissonSub = redissonSub;
        this.nodeId = nodeId;
    }

    @Override
    public void publish(String name, PubSubMessage msg) {
        msg.setNodeId(nodeId);
        redissonPub.getTopic(name).publish(msg);
    }

    @Override
    public <T extends PubSubMessage> void subscribe(String name, final PubSubListener<T> listener, Class<T> clazz) {
        RTopic<T> topic = redissonSub.getTopic(name);
        int regId = topic.addListener(new MessageListener<T>() {
            @Override
            public void onMessage(String channel, T msg) {
                if (!nodeId.equals(msg.getNodeId())) {
                    listener.onMessage(msg);
                }
            }
        });

        Queue<Integer> list = map.get(name);
        if (list == null) {
            list = new ConcurrentLinkedQueue<Integer>();
            Queue<Integer> oldList = map.putIfAbsent(name, list);
            if (oldList != null) {
                list = oldList;
            }
        }
        list.add(regId);
    }

    @Override
    public void unsubscribe(String name) {
        Queue<Integer> regIds = map.remove(name);
        RTopic<Object> topic = redissonSub.getTopic(name);
        for (Integer id : regIds) {
            topic.removeListener(id);
        }
    }

    @Override
    public void shutdown() {
    }

}
