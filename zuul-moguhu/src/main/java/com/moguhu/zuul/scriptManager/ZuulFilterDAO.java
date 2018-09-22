package com.moguhu.zuul.scriptManager;

import com.moguhu.baize.client.model.ComponentDto;

import java.util.List;

/**
 * Filter DAO
 * <p>
 * Created by xuefeihu on 18/9/19.
 */
public interface ZuulFilterDAO {

    /**
     * @return all filters active in the "canary" mode
     */
    List<ComponentDto> getAllCanaryFilters() throws Exception;

    /**
     * @return all active filters
     */
    List<ComponentDto> getAllActiveFilters() throws Exception;

    /**
     * @param compId
     * @return single component
     * @throws Exception
     */
    ComponentDto getSingleFilter(String compId) throws Exception;

}
