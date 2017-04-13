package cn.bridgeli.msg.push.entity;

import java.io.Serializable;


public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;
    private String content;
    private String userId;
    private String companyId;

    public Message() {
    }

    public Message(String type, String content, Integer userId) {
        this.type = type;
        this.content = content;
        if (userId != null) {
            this.userId = Integer.toString(userId);
        }
    }

    public Message(String type, String content, String userId) {
        this.type = type;
        this.content = content;
        this.userId = userId;
    }

    public Message(String type, String content, Integer userId, Integer companyId) {
        this.type = type;
        this.content = content;
        if (userId != null) {
            this.userId = Integer.toString(userId);
        }
        if (companyId != null) {
            this.companyId = Integer.toString(companyId);
        }
    }


    public Message(String type, String content, String userId, String companyId) {
        this.type = type;
        this.content = content;
        this.userId = userId;
        this.companyId = companyId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

}
