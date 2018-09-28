package com.moguhu.zuul.util;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Hosts Util
 *
 * Created by xuefeihu on 18/9/22.
 */
public class HostsUtil {

    public static String getRandomNode(List<String> hosts) {
        if (CollectionUtils.isEmpty(hosts)) {
            throw new RuntimeException("wrong manager node number.");
        }
        if (hosts.size() == 1) {
            return hosts.get(0);
        }
        Double random = Math.random() * hosts.size();
        return hosts.get(random.intValue());
    }

}
