package com.moguhu.zuul.zookeeper;

import com.google.common.collect.Lists;
import com.moguhu.baize.client.model.ApiDto;
import com.moguhu.baize.client.model.ApiGroupDto;
import com.moguhu.baize.client.model.ComponentDto;
import com.moguhu.zuul.context.NFRequestContext;
import com.moguhu.zuul.scriptManager.ZuulFilterPoller;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API Manager
 * <p>
 * 管理并监听对应服务下的API 及分组对应的组件. 并完成组件的更新. (具体的拉取动作, 由 ZuulFilterPoller完成, 以 gateServiceCode 的维度)
 * <p>
 * Created by xuefeihu on 18/9/18.
 */
public class ApiManager {

    private static final Map<String, ApiGroupDto> groupMap = new ConcurrentHashMap<>();
    
    private static final String URI_PATTERN = "/(.*)/(.*)";

    public static void putApi(ApiGroupDto group, ApiDto api) {
        if (null == group || null == api) {
            return;
        }
        String groupId = String.valueOf(group.getGroupId());
        if (groupMap.get(groupId) == null) {
            groupMap.put(groupId, group);
        }
        if (group.getApiList().contains(api)) {
            group.getApiList().remove(api);
        }
        processComponent(api);
        group.getApiList().add(api);
    }

    public static void removeApi(ApiGroupDto group, ApiDto api) {
        if (null == group || null == api) {
            return;
        }
        String groupId = String.valueOf(group.getGroupId());
        if (groupMap.get(groupId) == null) {
            return;
        }
        if (group.getApiList().contains(api)) {
            group.getApiList().remove(api);
        }
    }

    public static void putGroup(ApiGroupDto group) {
        if (null == group) {
            return;
        }
        String groupId = String.valueOf(group.getGroupId());
        groupMap.put(groupId, group);
        processComponent(group);
    }

    public static void removeGroup(ApiGroupDto group) {
        if (null == group) {
            return;
        }
        String groupId = String.valueOf(group.getGroupId());
        groupMap.remove(groupId);
    }

    /**
     * resolve group component info from ZuulFilterPoller. (contains api's component)
     *
     * @param group
     */
    private static void processComponent(ApiGroupDto group) {
        List<String> compIds = group.getCompIds();
        if (CollectionUtils.isNotEmpty(compIds)) {
            List<ComponentDto> componentList = Lists.newArrayList();
            compIds.forEach(compId -> {
                ComponentDto component = ZuulFilterPoller.getComponent(compId);
                componentList.add(component);
            });
            group.setComponentList(componentList);
        }
        List<ApiDto> apiList = group.getApiList();
        if (CollectionUtils.isNotEmpty(apiList)) {
            apiList.forEach(api -> {
                List<Long> apiCompIds = api.getCompIds();
                if (CollectionUtils.isNotEmpty(apiCompIds)) {
                    List<ComponentDto> apiComponentList = Lists.newArrayList();
                    apiCompIds.forEach(apiCompId -> {
                        ComponentDto component = ZuulFilterPoller.getComponent(String.valueOf(apiCompId));
                        apiComponentList.add(component);
                    });
                    api.setComponentList(apiComponentList);
                }
            });
        }
    }

    /**
     * resolve api component info from ZuulFilterPoller.
     *
     * @param api
     */
    private static void processComponent(ApiDto api) {
        List<Long> compIds = api.getCompIds();
        if (!CollectionUtils.isEmpty(compIds)) {
            List<ComponentDto> componentList = Lists.newArrayList();
            compIds.forEach(compId -> {
                ComponentDto component = ZuulFilterPoller.getComponent(String.valueOf(compId));
                componentList.add(component);
            });
            api.setComponentList(componentList);
        }
    }

    /**
     * 检查uri 是否合法, 并返回对应匹配的 ApiDto
     * @param uri
     * @return
     */
    public static ApiDto checkPermission(String uri) {
        Pattern p = Pattern.compile(URI_PATTERN);
        Matcher m = p.matcher(uri);
        if (!m.find()) {
            throw new RuntimeException("Uri was invalid!");
        }
        String groupPath = "/" + m.group(1);
        String apiPath = "/" + m.group(2);

        String groupId = "";
        Iterator<Map.Entry<String, ApiGroupDto>> iterator = groupMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ApiGroupDto> groupEntry = iterator.next();
            ApiGroupDto groupDto = groupEntry.getValue();
            if (groupPath.equals(groupDto.getPath())) {
                groupId = groupEntry.getKey();
                NFRequestContext.getCurrentContext().setBackendGroup(groupDto);
                break ;
            }
        }
        if (StringUtils.isEmpty(groupId)) {
            throw new RuntimeException("Uri was invalid!");
        }
        List<ApiDto> apiList = groupMap.get(groupId).getApiList();
        if (CollectionUtils.isEmpty(apiList)) {
            throw new RuntimeException("Uri was invalid!");
        }

        for (ApiDto apiDto : apiList) {
            if (apiPath.equals(apiDto.getPath())) {
                NFRequestContext.getCurrentContext().setBackendApi(apiDto);
                return apiDto;
            }
        }
        return null;
    }

}
