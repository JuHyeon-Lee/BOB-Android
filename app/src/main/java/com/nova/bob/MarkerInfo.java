package com.nova.bob;

/**
 * Created by iz000 on 2017-02-11.
 */

public class MarkerInfo {

    String phoneNumber;
    long createTime;

    public MarkerInfo(String phoneNumber){
        this.phoneNumber = phoneNumber;
        createTime = System.currentTimeMillis();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public long getCreateTime() {
        return createTime;
    }
}
