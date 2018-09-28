package com.moguhu.zuul.zookeeper.model;

/**
 * Api Path Model
 * <p>
 * Created by xuefeihu on 18/9/19.
 */
public class ApiPathModel {

    private String gateServiceCode;

    private String groupId;

    private String apiId;

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getGateServiceCode() {
        return gateServiceCode;
    }

    public void setGateServiceCode(String gateServiceCode) {
        this.gateServiceCode = gateServiceCode;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
