package com.moguhu.zuul.zookeeper.curator;

import com.moguhu.baize.client.constants.ZookeeperKey;
import com.moguhu.zuul.zookeeper.model.ApiPathModel;
import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API Path Parser
 * <p>
 * Created by xuefeihu on 18/9/19.
 */
public class ApiPathParser {

    private static final String PATTERN_API = ZookeeperKey.BAIZE_ZUUL + "/(.*)/apigroup/(.*)/(.*)";
    private static final String PATTERN_GROUP = ZookeeperKey.BAIZE_ZUUL + "/(.*)/apigroup/(.*)";

    public static ApiPathModel resolve(String path) {
        ApiPathModel apiPathModel = new ApiPathModel();
        if (StringUtils.isEmpty(path)) {
            return apiPathModel;
        }

        Pattern r = Pattern.compile(PATTERN_API);
        Matcher m = r.matcher(path);
        if (m.find()) {
            apiPathModel.setGateServiceCode(m.group(1));
            apiPathModel.setGroupId(m.group(2));
            apiPathModel.setApiId(m.group(3));
        } else {
            r = Pattern.compile(PATTERN_GROUP);
            m = r.matcher(path);
            if (m.find()) {
                apiPathModel.setGateServiceCode(m.group(1));
                apiPathModel.setGroupId(m.group(2));
            }
        }
        return apiPathModel;
    }

    public static void main(String[] args) {
        ApiPathModel resolve = resolve("/baize/zuul/payment/apigroup/4/45");
        System.out.println(resolve);
    }

}
