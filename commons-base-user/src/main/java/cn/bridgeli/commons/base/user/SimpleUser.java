package cn.bridgeli.commons.base.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  可出cookie中直接获取到的信息
 * @Description 基本用户信息
 */
public class SimpleUser {

    static Logger logger = LoggerFactory.getLogger(SimpleUser.class);

    private static final int DEFAULT_CURRENT_COMPANY_ID = -1;
    private static final int DEFAULT_USER_ID = -1;

    /** 匿名用户，游客 */
    public static final SimpleUser ANONYMOUS_USER = new Builder().userId(DEFAULT_USER_ID).currentCompanyId(DEFAULT_CURRENT_COMPANY_ID).build();

    private final Integer userId;
    private final Integer currentCompanyId;

    public static class Builder {
        private Integer userId;
        private Integer currentCompanyId;

        public Builder userId(Integer userId) {
            if(userId == null){
                userId = DEFAULT_USER_ID;
            }
            this.userId = userId;
            return this;
        }

        public Builder currentCompanyId(Integer currentCompanyId) {
            if(currentCompanyId == null){
                currentCompanyId = DEFAULT_CURRENT_COMPANY_ID;
            }
            this.currentCompanyId = currentCompanyId;
            return this;
        }

        public SimpleUser build() {
            return new SimpleUser(this);
        }

    }

    protected SimpleUser(Builder builder) {
        userId = builder.userId;
        currentCompanyId = builder.currentCompanyId;
    }

    static SimpleUser readSimpleUser(ByteBufferPlus bf) {
        return new Builder().userId(bf.getInt()).currentCompanyId(bf.getInt()).build();
    }

    static void write(ByteBufferPlus bf, SimpleUser su) {
        bf.putInt(su.userId);
        bf.putInt(su.currentCompanyId);
    }

    /**
     * 是否匿名用户
     * @return
     */
    public boolean isAnonymous() {
        return userId < 0;
    }

    /**
     * 得到用户id
     * @return
     */
    public int getUserId() {
        return userId;
    }

    public int getCurrentCompanyId() {
        return currentCompanyId;
    }

    @Override
    public String toString() {
        return "SimpleUser [userId=" + userId + ", currentCompanyId="
                + currentCompanyId + "]";
    }

}