package cn.bridgeli.msg.push.service;

import cn.bridgeli.msg.push.entity.Message;

public interface PushRemoteService {
    /**
     * 向页面推送消息
     * 
     * @param message
     */
    public void push(Message message);
}
