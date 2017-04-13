package cn.bridgeli.msg.push.impl.util;

public class PushUtils {

    public static String getCompanyIdUserId(String userId, String companyId) {
        return companyId + "_" + userId;
    }
}
