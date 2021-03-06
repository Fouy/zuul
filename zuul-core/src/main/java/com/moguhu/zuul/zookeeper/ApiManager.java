package com.moguhu.zuul.zookeeper;

import com.google.common.collect.Lists;
import com.moguhu.baize.client.model.ApiDto;
import com.moguhu.baize.client.model.ApiGroupDto;
import com.moguhu.baize.client.model.ComponentDto;
import com.moguhu.zuul.constants.ZuulConstants;
import com.moguhu.zuul.context.NFRequestContext;
import com.moguhu.zuul.exception.ZuulException;
import com.moguhu.zuul.scriptManager.ZuulFilterPoller;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    private static final Logger logger = LoggerFactory.getLogger(ApiManager.class);

    static DynamicStringProperty gateServiceCode = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.GATE_SERVICE_CODE, null);

    private static final Map<String, ApiGroupDto> groupMap = new ConcurrentHashMap<>();

    private static final String URI_PATTERN = "/(.*?)/(.*?)/(.*)";

    private static ApiManager INSTANCE;
    private volatile boolean running = true;

    private Thread checkerThread = new Thread("ApiManager") {
        @Override
        public void run() {
            while (running) {
                try {
                    if (MapUtils.isNotEmpty(groupMap)) {
                        groupMap.forEach((groupId, apiGroupDto) -> {
                            if (null != apiGroupDto && CollectionUtils.isNotEmpty(apiGroupDto.getCompIds())) {
                                List<ComponentDto> componentList = Lists.newArrayList();

                                List<Long> compIds = apiGroupDto.getCompIds();
                                if (CollectionUtils.isNotEmpty(compIds)) {
                                    compIds.forEach(compId -> {
                                        ComponentDto component = ZuulFilterPoller.getComponent(String.valueOf(compId));
                                        if (null != component) {
                                            componentList.add(component);
                                        }
                                    });
                                }
                                apiGroupDto.setComponentList(componentList);

                                if (CollectionUtils.isNotEmpty(apiGroupDto.getApiList())) {
                                    apiGroupDto.getApiList().forEach(apiDto -> {
                                        List<ComponentDto> apiComponentList = Lists.newArrayList();

                                        List<Long> apiCompIds = apiDto.getCompIds();
                                        if (CollectionUtils.isNotEmpty(apiCompIds)) {
                                            apiCompIds.forEach(compId -> {
                                                ComponentDto component = ZuulFilterPoller.getComponent(String.valueOf(compId));
                                                if (null != component) {
                                                    apiComponentList.add(component);
                                                }
                                            });
                                        }
                                        apiDto.setComponentList(apiComponentList);
                                    });
                                }

                            }
                        });
                    }
                } catch (Throwable e) {
                    logger.error("ApiManager run error! {}", e);
                }

                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    logger.error("ApiManager sleep error! {}", e);
                }
            }
        }

    };

    /**
     * 系统初始化时, 调用一次
     */
    public static void start() {
        if (INSTANCE == null) {
            synchronized (ApiManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ApiManager();
                }
            }
        }
    }

    private ApiManager() {
        checkerThread.start();
    }

    public static void putApi(ApiGroupDto group, ApiDto api) {
        if (null == group || null == api) {
            return;
        }
        String groupId = String.valueOf(group.getGroupId());
        if (groupMap.get(groupId) == null) {
            groupMap.put(groupId, group);
        }
        if (null == group.getApiList()) {
            group.setApiList(Lists.newArrayList());
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
        if (CollectionUtils.isEmpty(group.getApiList())) {
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
        List<Long> compIds = group.getCompIds();
        if (CollectionUtils.isNotEmpty(compIds)) {
            List<ComponentDto> componentList = Lists.newArrayList();
            compIds.forEach(compId -> {
                ComponentDto component = ZuulFilterPoller.getComponent(String.valueOf(compId));
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
     *
     * @param uri
     * @return
     */
    public static ApiDto checkPermission(String uri) throws ZuulException {
        Pattern p = Pattern.compile(URI_PATTERN);
        Matcher m = p.matcher(uri);
        if (!m.find()) {
            throw new ZuulException("Uri was invalid", HttpStatus.SC_OK, "");
        }
        String serviceCode = m.group(1);
        String groupPath = "/" + m.group(2);
        String apiPath = groupPath + "/" + m.group(3);
        if (!gateServiceCode.get().equals(serviceCode)) {
            throw new ZuulException("serviceCode was invalid", HttpStatus.SC_OK, "");
        }

        String groupId = "";
        Iterator<Map.Entry<String, ApiGroupDto>> iterator = groupMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ApiGroupDto> groupEntry = iterator.next();
            ApiGroupDto groupDto = groupEntry.getValue();
            if (groupPath.equals(groupDto.getPath())) {
                groupId = groupEntry.getKey();
                NFRequestContext.getCurrentContext().setBackendGroup(groupDto);
                break;
            }
        }
        if (StringUtils.isEmpty(groupId)) {
            throw new ZuulException("Uri was invalid, cannot find group", HttpStatus.SC_OK, "");
        }
        List<ApiDto> apiList = groupMap.get(groupId).getApiList();
        if (CollectionUtils.isEmpty(apiList)) {
            throw new ZuulException("Uri was invalid, empty api list", HttpStatus.SC_OK, "");
        }

        for (ApiDto apiDto : apiList) {
            if (apiPath.equals(apiDto.getPath())) {
                NFRequestContext.getCurrentContext().setBackendApi(apiDto);
                return apiDto;
            }
        }
        throw new ZuulException("Uri was invalid, cannot find uri pattern", HttpStatus.SC_OK, "");
    }

}
