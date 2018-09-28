package com.moguhu.zuul.scriptManager;

import com.moguhu.baize.client.constants.ZookeeperKey;
import com.moguhu.zuul.constants.ZuulConstants;
import com.moguhu.zuul.zookeeper.curator.CuratorClient;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import java.util.List;

/**
 * Http ZuulFilterDAO Builder
 */
public class HttpZuulFilterDAOBuilder implements ZuulFilterDAOBuilder {

    private static final DynamicStringProperty gateServiceCode = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.GATE_SERVICE_CODE, null);

    public HttpZuulFilterDAOBuilder() {
    }

    @Override
    public ZuulFilterDAO build() {
        List<String> managerNodes = CuratorClient.getInstance().getChildren(ZookeeperKey.BAIZE_MANAGER_NODES);
        return new HttpZuulFilterDAO(gateServiceCode.get(), managerNodes);
    }

}
