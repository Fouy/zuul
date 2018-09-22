package com.moguhu.zuul.plugins;

import com.netflix.servo.monitor.DynamicTimer;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.Stopwatch;
import com.netflix.servo.tag.InjectableTag;
import com.netflix.servo.tag.Tag;
import com.moguhu.zuul.monitoring.TracerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Plugin to hook up Servo Tracers
 */
public class Tracer extends TracerFactory {

    static List<Tag> tags = new ArrayList<Tag>(2);

    static {
        tags.add(InjectableTag.HOSTNAME);
        tags.add(InjectableTag.IP);
    }

    @Override

    public com.moguhu.zuul.monitoring.Tracer startMicroTracer(String name) {
        return new ServoTracer(name);
    }

    class ServoTracer implements com.moguhu.zuul.monitoring.Tracer {

        final MonitorConfig config;
        final Stopwatch stopwatch;

        private ServoTracer(String name) {
            config = MonitorConfig.builder(name).withTags(tags).build();
            stopwatch = DynamicTimer.start(config, TimeUnit.MICROSECONDS);
        }

        @Override
        public void stopAndLog() {
            DynamicTimer.record(config, stopwatch.getDuration());
        }

        @Override
        public void setName(String name) {

        }
    }
}
