package com.moguhu.zuul.monitoring.stat;

import com.moguhu.zuul.monitoring.common.MemoryStats;
import com.moguhu.zuul.monitoring.common.Stats;
import com.moguhu.zuul.monitoring.common.StatsGetter;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;


public class MemoryStatsGetter implements StatsGetter {

    MemoryMXBean bean = ManagementFactory.getMemoryMXBean();

    @Override
    public Stats get() {
        MemoryStats s = new MemoryStats();

        MemoryUsage u = bean.getHeapMemoryUsage();
        s.setHeapCommitedMemory(u.getCommitted());
        s.setHeapUsedMemory(u.getUsed());
        s.setHeapMaxMemory(u.getMax());

        u = bean.getNonHeapMemoryUsage();
        s.setNonHeapCommitedMemory(u.getCommitted());
        s.setNonHeapUsedMemory(u.getUsed());
        s.setNonHeapMaxMemory(u.getMax());

        return s;
    }

}
