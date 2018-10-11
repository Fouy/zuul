package com.moguhu.zuul;

import com.moguhu.baize.client.constants.ZookeeperKey;
import com.moguhu.zuul.constants.ZuulConstants;
import com.moguhu.zuul.context.NFRequestContext;
import com.moguhu.zuul.context.RequestContext;
import com.moguhu.zuul.groovy.GroovyCompiler;
import com.moguhu.zuul.groovy.GroovyFileFilter;
import com.moguhu.zuul.scriptManager.ZuulFilterPoller;
import com.moguhu.zuul.zookeeper.ApiManager;
import com.moguhu.zuul.zookeeper.curator.ApiTreeCacheListener;
import com.moguhu.zuul.zookeeper.curator.CuratorClient;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class StartServer implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(StartServer.class);

    static DynamicStringProperty gateServiceCode = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.GATE_SERVICE_CODE, null);

    public StartServer() {
        if (StringUtils.isEmpty(gateServiceCode.get())) {
            logger.error("GateWay serviceCode was not setting, Server Shutdown!");
            System.exit(-1);
        }

        System.setProperty(DynamicPropertyFactory.ENABLE_JMX, "true");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            // TODO 初始化 infobord 监控信息, 先跳过, 后续补上

            initMonitor();
            initZookeeper();
            initZuul();

//            ApplicationInfoManager.getInstance().setInstanceStatus(InstanceInfo.InstanceStatus.UP);
        } catch (Exception e) {
            logger.error("Error while initializing zuul gateway.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        FilterFileManager.shutdown();
    }

    private void initMonitor() {
    }

    private void initZookeeper() {
        logger.info("Init Zookeeper Client.");
        CuratorClient.getInstance();
        CuratorFramework client = CuratorClient.getInstance().getClient();
        String path = ZookeeperKey.BAIZE_ZUUL + "/" + gateServiceCode.get() + "/" + ZookeeperKey.SERVICECODE_APIGROUP;
        TreeCache apiGroupTree = new TreeCache(client, path);
        apiGroupTree.getListenable().addListener(new ApiTreeCacheListener());
        try {
            apiGroupTree.start();
        } catch (Exception e) {
            logger.error("Init Zookeeper Client, Add Listener Error, {}", e);
            System.exit(-1);
        }
    }

    private void initZuul() throws Exception {
        RequestContext.setContextClass(NFRequestContext.class);

        logger.info("Starting Groovy Filter file manager");
        final AbstractConfiguration config = ConfigurationManager.getConfigInstance();

        final String preFiltersPath = config.getString(ZuulConstants.ZUUL_FILTER_PRE_PATH);
        final String postFiltersPath = config.getString(ZuulConstants.ZUUL_FILTER_POST_PATH);
        final String routeFiltersPath = config.getString(ZuulConstants.ZUUL_FILTER_ROUTE_PATH);
        final String errorFiltersPath = config.getString(ZuulConstants.ZUUL_FILTER_ERROR_PATH);
        final String customPath = config.getString(ZuulConstants.ZUUL_FILTER_CUSTOM_PATH);

        FilterLoader.getInstance().setCompiler(new GroovyCompiler());
        FilterFileManager.setFilenameFilter(new GroovyFileFilter());
        if (customPath == null) {
            FilterFileManager.init(5, preFiltersPath, postFiltersPath, routeFiltersPath, errorFiltersPath);
        } else {
            FilterFileManager.init(5, preFiltersPath, postFiltersPath, routeFiltersPath, errorFiltersPath, customPath);
        }

        // synchronze manager components
        startZuulFilterPoller();

        // synchronze api manager
        startApiManager();
        logger.info("Groovy Filter file manager started");
    }

    private void startZuulFilterPoller() {
        ZuulFilterPoller.start();
        logger.info("ZuulFilterPoller Started.");
    }

    private void startApiManager() {
        ApiManager.start();
        logger.info("ApiManager Started.");
    }

}
