package com.moguhu.zuul.component;

import java.util.Collection;
import java.util.List;

public interface RouteLocator {

    /**
     * Ignored route paths (or patterns), if any.
     */
    Collection<String> getIgnoredPaths();

    /**
     * A map of route path (pattern) to location (e.g. service id or URL).
     */
    List<Route> getRoutes();

    /**
     * Maps a path to an actual route with full metadata.
     */
    Route getMatchingRoute(String path);

}
