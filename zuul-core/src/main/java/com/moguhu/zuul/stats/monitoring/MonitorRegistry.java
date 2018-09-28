package com.moguhu.zuul.stats.monitoring;

/**
 * Registry to register a Counter. a Monitor publisher should  be set to get counter information.
 */
public class MonitorRegistry {

    private static final MonitorRegistry instance = new MonitorRegistry();
    private Monitor publisher;

    /**
     * A Monitor implementation should be set here
     *
     * @param publisher
     */
    public void setPublisher(Monitor publisher) {
        this.publisher = publisher;
    }


    public static MonitorRegistry getInstance() {
        return instance;
    }

    public void registerObject(NamedCount monitorObj) {
        if (publisher != null) publisher.register(monitorObj);
    }
}
