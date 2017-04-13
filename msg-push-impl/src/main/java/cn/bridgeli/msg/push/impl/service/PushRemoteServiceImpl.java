package cn.bridgeli.msg.push.impl.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.bridgeli.msg.push.entity.Message;
import cn.bridgeli.msg.push.impl.server.PushServer;
import cn.bridgeli.msg.push.service.PushRemoteService;

@Service
public class PushRemoteServiceImpl implements PushRemoteService {

    private Logger logger = LoggerFactory.getLogger(PushRemoteServiceImpl.class);

    @Override
    public void push(Message message) {
        PushServer.pushServer.push(message);
    }

}
