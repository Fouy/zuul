package com.moguhu.zuul.stats;

import com.moguhu.zuul.stats.monitoring.MonitorRegistry;
import com.moguhu.zuul.stats.monitoring.NamedCount;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.annotations.MonitorTags;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.TagList;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple Epic counter with a name and a count.
 *
 * @author mhawthorne
 */
public class NamedCountingMonitor implements NamedCount {

    private final String name;

    @MonitorTags
    TagList tagList;

    @Monitor(name = "count", type = DataSourceType.COUNTER)
    private final AtomicLong count = new AtomicLong();

    public NamedCountingMonitor(String name) {
        this.name = name;
        tagList = BasicTagList.of("ID", name);
    }

    /**
     * reguisters this objects
     *
     * @return
     */
    public NamedCountingMonitor register() {
        MonitorRegistry.getInstance().registerObject(this);
        return this;
    }

    /**
     * increments the counter
     *
     * @return
     */
    public long increment() {
        return this.count.incrementAndGet();
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @return the current count
     */
    public long getCount() {
        return this.count.get();
    }

}
