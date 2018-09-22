package com.moguhu.zuul.stats;

import com.moguhu.zuul.stats.monitoring.MonitorRegistry;
import com.moguhu.zuul.context.RequestContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager to handle Error Statistics
 */
public class ErrorStatsManager {
    ConcurrentHashMap<String, ConcurrentHashMap<String, ErrorStatsData>> routeMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ErrorStatsData>>();
    final static ErrorStatsManager INSTANCE = new ErrorStatsManager();

    /**
     * @return Singleton
     */
    public static ErrorStatsManager getManager() {
        return INSTANCE;
    }


    /**
     * @param route
     * @param cause
     * @return data structure for holding count information for a route and cause
     */
    public ErrorStatsData getStats(String route, String cause) {
        Map<String, ErrorStatsData> map = routeMap.get(route);
        if (map == null) return null;
        return map.get(cause);
    }

    /**
     * updates count for the given route and error cause
     *
     * @param route
     * @param cause
     */
    public void putStats(String route, String cause) {
        if (route == null) route = RequestContext.getCurrentContext().getRequest().getRequestURI();
        if (route == null) route = "UNKNOWN_ROUTE";
        route = route.replace("/", "_");
        ConcurrentHashMap<String, ErrorStatsData> statsMap = routeMap.get(route);
        if (statsMap == null) {
            statsMap = new ConcurrentHashMap<String, ErrorStatsData>();
            routeMap.putIfAbsent(route, statsMap);
        }
        ErrorStatsData sd = statsMap.get(cause);
        if (sd == null) {
            sd = new ErrorStatsData(route, cause);
            ErrorStatsData sd1 = statsMap.putIfAbsent(cause, sd);
            if (sd1 != null) {
                sd = sd1;
            } else {
                MonitorRegistry.getInstance().registerObject(sd);
            }
        }
        sd.update();
    }

}

