package com.moguhu.zuul.scriptManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.moguhu.baize.client.model.ComponentDto;
import com.moguhu.zuul.util.ApiConnector;
import com.moguhu.zuul.util.HostsUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 按照 gateServiceCode的维度拉取组件 (manager 暂定全部拉取)
 * <p>
 * Created by xuefeihu on 18/9/21.
 */
public class HttpZuulFilterDAO implements ZuulFilterDAO {

    private static final Logger logger = LoggerFactory.getLogger(HttpZuulFilterDAO.class);

    private static final String MANAGER_PREFIX = "/manager/%s";

    private static final String COMP_CODE_PATTERN = "(.*):(.*):(.*)";

    private static final String HOST_PROTOCOL = "http://";

    private List<String> managerNodes = Lists.newArrayList();
    private String gateServiceCode;

    public HttpZuulFilterDAO(String gateServiceCode, List<String> managerNodes) {
        this.gateServiceCode = gateServiceCode;
        this.managerNodes = managerNodes;
    }

    @Override
    public List<ComponentDto> getAllCanaryFilters() throws Exception {
        List<ComponentDto> list = Lists.newArrayList();

        String url = HOST_PROTOCOL + HostsUtil.getRandomNode(managerNodes) + String.format(MANAGER_PREFIX, "allcomponents");
        List<NameValuePair> pairs = Lists.newArrayList();
        pairs.add(new BasicNameValuePair("gateServiceCode", gateServiceCode));
        pairs.add(new BasicNameValuePair("type", "canary"));
        String responseStr = ApiConnector.post(url, pairs);
        if (StringUtils.isNotEmpty(responseStr)) {
            JSONObject ajaxResult = JSON.parseObject(responseStr);
            Integer code = ajaxResult.getInteger("code");
            if (1000 == code) {
                Object data = ajaxResult.get("data");
                list = JSON.parseObject(JSON.toJSONString(data), new TypeReference<List<ComponentDto>>() {
                });
                list.forEach(component -> convertComponent(component));
            }
        }
        return list;
    }


    @Override
    public List<ComponentDto> getAllActiveFilters() throws Exception {
        List<ComponentDto> list = Lists.newArrayList();

        String url = HOST_PROTOCOL + HostsUtil.getRandomNode(managerNodes) + String.format(MANAGER_PREFIX, "allcomponents");
        List<NameValuePair> pairs = Lists.newArrayList();
        pairs.add(new BasicNameValuePair("gateServiceCode", gateServiceCode));
        pairs.add(new BasicNameValuePair("type", "active"));
        String responseStr = ApiConnector.post(url, pairs);
        if (StringUtils.isNotEmpty(responseStr)) {
            JSONObject ajaxResult = JSON.parseObject(responseStr);
            Integer code = ajaxResult.getInteger("code");
            if (1000 == code) {
                Object data = ajaxResult.get("data");
                list = JSON.parseObject(JSON.toJSONString(data), new TypeReference<List<ComponentDto>>() {
                });
                list.forEach(component -> convertComponent(component));
            }
        }
        return list;
    }

    @Override
    public ComponentDto getSingleFilter(String compId) throws Exception {
        ComponentDto component = new ComponentDto();

        String url = HOST_PROTOCOL + HostsUtil.getRandomNode(managerNodes) + String.format(MANAGER_PREFIX, "getcomponent");
        List<NameValuePair> pairs = Lists.newArrayList();
        pairs.add(new BasicNameValuePair("gateServiceCode", gateServiceCode));
        pairs.add(new BasicNameValuePair("compId", compId));
        String responseStr = ApiConnector.post(url, pairs);
        if (StringUtils.isNotEmpty(responseStr)) {
            JSONObject ajaxResult = JSON.parseObject(responseStr);
            Integer code = ajaxResult.getInteger("code");
            if (1000 == code) {
                Object data = ajaxResult.get("data");
                component = JSON.parseObject(JSON.toJSONString(data), new TypeReference<ComponentDto>() {
                });
                convertComponent(component);
            }
        }
        return component;
    }

    private void convertComponent(ComponentDto component) {
        String compCode = component.getCompCode();
        if (StringUtils.isNotEmpty(compCode)) {
            Pattern p = Pattern.compile(COMP_CODE_PATTERN);
            Matcher m = p.matcher(compCode);
            if (m.find()) {
                component.setFileName(m.group(2));
            }
        }
    }

}

