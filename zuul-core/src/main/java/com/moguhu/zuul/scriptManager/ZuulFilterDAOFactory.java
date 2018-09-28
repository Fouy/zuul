package com.moguhu.zuul.scriptManager;

import com.google.common.collect.Maps;
import com.moguhu.zuul.constants.ZuulConstants;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import java.util.concurrent.ConcurrentMap;

public class ZuulFilterDAOFactory {

    private static final DynamicStringProperty daoType = DynamicPropertyFactory.getInstance().getStringProperty(ZuulConstants.ZUUL_FILTER_DAO_TYPE, "http");

    private static ConcurrentMap<String, ZuulFilterDAO> daoCache = Maps.newConcurrentMap();

    private ZuulFilterDAOFactory() {
    }

    public static ZuulFilterDAO getZuulFilterDao() {
        ZuulFilterDAO dao = daoCache.get(daoType.get());
        if (dao != null) {
            return dao;
        }

        if ("http".equalsIgnoreCase(daoType.get())) {
            dao = new HttpZuulFilterDAOBuilder().build();
        } else {
            throw new RuntimeException("Zuul Filter DAO Type error.");
        }
        daoCache.putIfAbsent(daoType.get(), dao);
        return dao;
    }

    public static String getCurrentType() {
        return daoType.get();
    }

}
