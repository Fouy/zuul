package com.moguhu.zuul.plugins;

import com.netflix.servo.monitor.Monitors;
import com.moguhu.zuul.stats.monitoring.Monitor;
import com.moguhu.zuul.stats.monitoring.NamedCount;

/**
 * implementation to hook up the Servo Monitors to register Named counters
 */
public class ServoMonitor implements Monitor {
    @Override
    public void register(NamedCount monitorObj) {
        Monitors.registerObject(monitorObj);
    }
}
