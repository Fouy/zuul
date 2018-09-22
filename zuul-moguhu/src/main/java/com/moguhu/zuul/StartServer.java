package com.moguhu.zuul;

import com.moguhu.baize.client.constants.ZookeeperKey;
import com.moguhu.zuul.constants.ZuulConstants;
import com.moguhu.zuul.context.NFRequestContext;
import com.moguhu.zuul.context.RequestContext;
import com.moguhu.zuul.groovy.GroovyCompiler;
import com.moguhu.zuul.groovy.GroovyFileFilter;
import com.moguhu.zuul.monitoring.CounterFactory;
import com.moguhu.zuul.monitoring.TracerFactory;
import com.moguhu.zuul.plugins.Counter;
import com.moguhu.zuul.plugins.MetricPoller;
import com.moguhu.zuul.plugins.ServoMonitor;
import com.moguhu.zuul.plugins.Tracer;
import com.moguhu.zuul.scriptManager.ZuulFilterPoller;
import com.moguhu.zuul.stats.monitoring.MonitorRegistry;
import com.moguhu.zuul.zookeeper.curator.ApiTreeCacheListener;
import com.moguhu.zuul.zookeeper.curator.CuratorClient;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.servo.util.ThreadCpuStats;
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

            ApplicationInfoManager.getInstance().setInstanceStatus(InstanceInfo.InstanceStatus.UP);
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
        logger.info("Registering Servo Monitor");
        MonitorRegistry.getInstance().setPublisher(new ServoMonitor());

        logger.info("Starting Poller");
        MetricPoller.startPoller();

        logger.info("Registering Servo Tracer");
        TracerFactory.initialize(new Tracer());

        logger.info("Registering Servo Counter");
        CounterFactory.initialize(new Counter());

        logger.info("Starting CPU stats");
        final ThreadCpuStats stats = ThreadCpuStats.getInstance();
        stats.start();
    }

    private void initZookeeper() {
        logger.info("Init Zookeeper Client.");
        CuratorClient.getInstance();
        CuratorFramework client = CuratorClient.getInstance().getClient();
        TreeCache apiGroupTree = new TreeCache(client, ZookeeperKey.BAIZE_ZUUL + "/" + gateServiceCode + ZookeeperKey.SERVICECODE_APIGROUP);
        apiGroupTree.getListenable().addListener(new ApiTreeCacheListener());
    }

    private void initZuul() throws Exception {
        RequestContext.setContextClass(NFRequestContext.class);

        CounterFactory.initialize(new Counter());
        TracerFactory.initialize(new Tracer());

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
        logger.info("Groovy Filter file manager started");
    }

    private void startZuulFilterPoller() {
        ZuulFilterPoller.start();
        logger.info("ZuulFilterPoller Started.");
    }

}
