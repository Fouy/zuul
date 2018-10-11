package com.moguhu.zuul.monitoring.stat;

import com.moguhu.zuul.monitoring.common.ClassStats;
import com.moguhu.zuul.monitoring.common.Stats;
import com.moguhu.zuul.monitoring.common.StatsGetter;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;


public class ClassStatsGetter implements StatsGetter {

    ClassLoadingMXBean bean = ManagementFactory.getClassLoadingMXBean();

    @Override
    public Stats get() {
        ClassStats s = new ClassStats();
        s.setCurrentClassCount(bean.getLoadedClassCount());
        s.setBeenLoadedClassCount(bean.getTotalLoadedClassCount());
        s.setBeenUnloadedClassCount(bean.getUnloadedClassCount());
        return s;
    }
}
