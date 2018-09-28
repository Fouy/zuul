package com.moguhu.zuul.zookeeper.curator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.moguhu.baize.client.model.ApiDto;
import com.moguhu.baize.client.model.ApiGroupDto;
import com.moguhu.baize.client.utils.ZookeeperPathBuilder;
import com.moguhu.zuul.constants.ZuulConstants;
import com.moguhu.zuul.zookeeper.ApiManager;
import com.moguhu.zuul.zookeeper.model.ApiPathModel;
import com.netflix.config.ConfigurationManager;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * API zk Listener
 * <p>
 * Created by xuefeihu on 18/9/19.
 */
public class ApiTreeCacheListener implements TreeCacheListener {

    private static final Logger logger = LoggerFactory.getLogger(ApiTreeCacheListener.class);

    private static final String gateServiceCode = ConfigurationManager.getConfigInstance().getString(ZuulConstants.GATE_SERVICE_CODE);

    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        ChildData data = event.getData();
        if (data != null) {
            String addedPath = data.getPath();
            ApiPathModel apiPathModel = ApiPathParser.resolve(addedPath);

            switch (event.getType()) {
                case NODE_ADDED:
                    if (StringUtils.isNotEmpty(apiPathModel.getGroupId()) && StringUtils.isNotEmpty(apiPathModel.getApiId())) { // API创建
                        String dataStr = new String(data.getData());
                        ApiDto api = JSON.parseObject(dataStr, new TypeReference<ApiDto>() {
                        });

                        String groupPath = ZookeeperPathBuilder.buildGroupPath(gateServiceCode, Long.parseLong(apiPathModel.getGroupId()));
                        String groupInfoStr = CuratorClient.getInstance().readNode(groupPath);
                        ApiGroupDto apiGroup = JSON.parseObject(groupInfoStr, new TypeReference<ApiGroupDto>() {
                        });
                        ApiManager.putApi(apiGroup, api);
                        logger.info(" >>>>>>>>>>> new API added to GateWay, apiInfo = {}", JSON.toJSONString(api));
                    } else if (StringUtils.isNotEmpty(apiPathModel.getGroupId()) && StringUtils.isEmpty(apiPathModel.getApiId())) { // Group创建
                        String groupInfoStr = new String(data.getData());
                        ApiGroupDto apiGroup = JSON.parseObject(groupInfoStr, new TypeReference<ApiGroupDto>() {
                        });
                        String groupPath = data.getPath();
                        List<String> apiPaths = CuratorClient.getInstance().getChildren(groupPath);
                        if (CollectionUtils.isNotEmpty(apiPaths)) {
                            apiGroup.setApiList(Lists.newArrayList());
                            apiPaths.forEach(apiPath -> {
                                try {
                                    apiPath = groupPath + "/" + apiPath;
                                    String apiInfoStr = CuratorClient.getInstance().readNode(apiPath);
                                    ApiDto api = JSON.parseObject(apiInfoStr, new TypeReference<ApiDto>() {
                                    });
                                    apiGroup.getApiList().add(api);
                                } catch (Exception e) {
                                    logger.error(" ############# group added, get apiInfo error, {}", e);
                                }
                            });
                        }
                        ApiManager.putGroup(apiGroup);
                        logger.info(" >>>>>>>>>>> new Group added to GateWay, groupInfo = {}", JSON.toJSONString(apiGroup));
                    }
                    break;
                case NODE_UPDATED:
                    if (StringUtils.isNotEmpty(apiPathModel.getGroupId()) && StringUtils.isNotEmpty(apiPathModel.getApiId())) { // API变更
                        String dataStr = new String(data.getData());
                        ApiDto api = JSON.parseObject(dataStr, new TypeReference<ApiDto>() {
                        });

                        String groupPath = ZookeeperPathBuilder.buildGroupPath(gateServiceCode, Long.parseLong(apiPathModel.getGroupId()));
                        String groupInfoStr = CuratorClient.getInstance().readNode(groupPath);
                        ApiGroupDto apiGroup = JSON.parseObject(groupInfoStr, new TypeReference<ApiGroupDto>() {
                        });
                        ApiManager.putApi(apiGroup, api);
                        logger.info(" >>>>>>>>>>> new API added to GateWay, apiInfo = {}", JSON.toJSONString(api));
                    } else if (StringUtils.isNotEmpty(apiPathModel.getGroupId()) && StringUtils.isEmpty(apiPathModel.getApiId())) { // Group 变更
                        String groupInfoStr = new String(data.getData());
                        ApiGroupDto apiGroup = JSON.parseObject(groupInfoStr, new TypeReference<ApiGroupDto>() {
                        });
                        String groupPath = data.getPath();
                        List<String> apiPaths = CuratorClient.getInstance().getChildren(groupPath);
                        if (CollectionUtils.isNotEmpty(apiPaths)) {
                            apiGroup.setApiList(Lists.newArrayList());
                            apiPaths.forEach(apiPath -> {
                                try {
                                    apiPath = groupPath + "/" + apiPath;
                                    String apiInfoStr = CuratorClient.getInstance().readNode(apiPath);
                                    ApiDto api = JSON.parseObject(apiInfoStr, new TypeReference<ApiDto>() {
                                    });
                                    apiGroup.getApiList().add(api);
                                } catch (Exception e) {
                                    logger.error(" ############# group added, get apiInfo error, {}", e);
                                }
                            });
                        }
                        ApiManager.putGroup(apiGroup);
                        logger.info(" >>>>>>>>>>> new Group added to GateWay, groupInfo = {}", JSON.toJSONString(apiGroup));
                    }
                    break;
                case NODE_REMOVED:
                    if (StringUtils.isNotEmpty(apiPathModel.getGroupId()) && StringUtils.isNotEmpty(apiPathModel.getApiId())) { // API删除
                        String dataStr = new String(data.getData());
                        ApiDto api = JSON.parseObject(dataStr, new TypeReference<ApiDto>() {
                        });

                        String groupPath = ZookeeperPathBuilder.buildGroupPath(gateServiceCode, Long.parseLong(apiPathModel.getGroupId()));
                        String groupInfoStr = CuratorClient.getInstance().readNode(groupPath);
                        ApiGroupDto apiGroup = JSON.parseObject(groupInfoStr, new TypeReference<ApiGroupDto>() {
                        });
                        ApiManager.removeApi(apiGroup, api);
                        logger.info(" >>>>>>>>>>> Api removed from GateWay, apiInfo = {}", JSON.toJSONString(api));
                    } else if (StringUtils.isNotEmpty(apiPathModel.getGroupId()) && StringUtils.isEmpty(apiPathModel.getApiId())) { // Group删除
                        String groupInfoStr = new String(data.getData());
                        ApiGroupDto apiGroup = JSON.parseObject(groupInfoStr, new TypeReference<ApiGroupDto>() {
                        });
                        ApiManager.removeGroup(apiGroup);
                        logger.info(" >>>>>>>>>>> Group removed from GateWay, groupInfo = {}", JSON.toJSONString(apiGroup));
                    }
                    break;

                default:
                    break;
            }
        } else {
            logger.warn(" Zookeeper watcher data is null : eventType = {}", event.getType());
        }
    }

}
