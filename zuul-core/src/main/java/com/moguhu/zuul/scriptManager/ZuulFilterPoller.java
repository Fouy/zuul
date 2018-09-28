package com.moguhu.zuul.scriptManager;

import com.google.common.collect.Maps;
import com.moguhu.baize.client.model.ComponentDto;
import com.moguhu.zuul.constants.ZuulConstants;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Poller 定时通过HTTP接口全量同步组件
 */
public class ZuulFilterPoller {

    private static final Logger logger = LoggerFactory.getLogger(ZuulFilterPoller.class);

    private static Map<String, ComponentDto> runningComponents = Maps.newHashMap();

    static DynamicLongProperty pollerInterval = DynamicPropertyFactory.getInstance().getLongProperty(ZuulConstants.ZUUL_FILTER_POLLER_INTERVAL, 30000);
    static DynamicBooleanProperty active = DynamicPropertyFactory.getInstance().getBooleanProperty(ZuulConstants.ZUUL_USE_ACTIVE_FILTERS, true);
    static DynamicBooleanProperty canary = DynamicPropertyFactory.getInstance().getBooleanProperty(ZuulConstants.ZUUL_USE_CANARY_FILTERS, false);

    static DynamicStringProperty preFiltersPath = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.ZUUL_FILTER_PRE_PATH, null);
    static DynamicStringProperty routeFiltersPath = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.ZUUL_FILTER_ROUTE_PATH, null);
    static DynamicStringProperty postFiltersPath = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.ZUUL_FILTER_POST_PATH, null);
    static DynamicStringProperty errorFiltersPath = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.ZUUL_FILTER_ERROR_PATH, null);
    static DynamicStringProperty customFiltersPath = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.ZUUL_FILTER_CUSTOM_PATH, null);

    private static ZuulFilterPoller INSTANCE;
    private volatile boolean running = true;

    private Thread checkerThread = new Thread("ZuulFilterPoller") {
        @Override
        public void run() {
            while (running) {
                try {
                    if (canary.get()) {
                        HashMap<String, ComponentDto> setFilters = new HashMap<>();

                        List<ComponentDto> activeScripts = ZuulFilterDAOFactory.getZuulFilterDao().getAllActiveFilters();
                        if (activeScripts != null) {
                            for (ComponentDto newComponent : activeScripts) {
                                setFilters.put(String.valueOf(newComponent.getCompId()), newComponent);
                            }
                        }

                        List<ComponentDto> canaryScripts = ZuulFilterDAOFactory.getZuulFilterDao().getAllCanaryFilters();
                        if (canaryScripts != null) {
                            for (ComponentDto newComponent : canaryScripts) {
                                setFilters.put(String.valueOf(newComponent.getCompId()), newComponent);
                            }
                        }
                        for (ComponentDto next : setFilters.values()) {
                            doCompCheck(next);
                        }
                    } else if (active.get()) {
                        List<ComponentDto> newComponents = ZuulFilterDAOFactory.getZuulFilterDao().getAllActiveFilters();
                        if (CollectionUtils.isEmpty(newComponents)) {
                            logger.warn("ZuulFilterPoller Warnning: There has NO active Component!");
                            return;
                        }
                        for (ComponentDto component : newComponents) {
                            doCompCheck(component);
                        }
                    }
                } catch (Throwable e) {
                    logger.error("ZuulFilterPoller run error! {}", e);
                }

                try {
                    sleep(pollerInterval.get());
                } catch (InterruptedException e) {
                    logger.error("ZuulFilterPoller sleep error! {}", e);
                }
            }
        }

    };

    /**
     * Starts the check against the ZuulFilter data store for changed or new filters.
     */
    public static void start() {
        if (INSTANCE == null) {
            synchronized (ZuulFilterPoller.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ZuulFilterPoller();
                }
            }
        }
    }

    public ZuulFilterPoller() {
        checkerThread.start();
    }

    public static ComponentDto getComponent(String compId) {
        return runningComponents.get(compId);
    }

    public void stop() {
        this.running = false;
    }

    /**
     * @return a Singleton
     */
    public static ZuulFilterPoller getInstance() {
        return INSTANCE;
    }

    private static void doCompCheck(ComponentDto newComponent) throws IOException {
        ComponentDto existingComp = runningComponents.get(String.valueOf(newComponent.getCompId()));
        if (existingComp == null || !existingComp.equals(newComponent)) {
            logger.info("adding filter to disk compCode = {}", newComponent.getCompCode());
            writeCompToDisk(newComponent);
            runningComponents.put(String.valueOf(newComponent.getCompId()), newComponent);
        }
    }

    private static void writeCompToDisk(ComponentDto newComponent) throws IOException {
        String filterType = newComponent.getExecPosition();

        String path = preFiltersPath.get();
        if (filterType.equalsIgnoreCase("post")) {
            path = postFiltersPath.get();
        } else if (filterType.equalsIgnoreCase("route")) {
            path = routeFiltersPath.get();
        } else if (filterType.equalsIgnoreCase("error")) {
            path = errorFiltersPath.get();
        } else if (!filterType.equalsIgnoreCase("pre") && customFiltersPath.get() != null) {
            path = customFiltersPath.get();
        }

        File f = new File(path, newComponent.getFileName() + ".groovy");
        FileWriter file = new FileWriter(f);
        BufferedWriter out = new BufferedWriter(file);
        out.write(newComponent.getCompContent());
        out.close();
        file.close();
        logger.info("filter written {}", f.getPath());
    }

}
