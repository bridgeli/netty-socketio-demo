package cn.bridgeli.msg.push.impl.entity;

import java.io.Serializable;


public class PushUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private int userId;
    private int companyId;


    public PushUser() {
    }

    public PushUser(int userId) {
        this.userId = userId;
    }

    public PushUser(int userId, int companyId) {
        this.userId = userId;
        this.companyId = companyId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }
}
