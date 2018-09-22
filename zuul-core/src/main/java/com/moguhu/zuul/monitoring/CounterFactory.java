package com.moguhu.zuul.monitoring;

/**
 * Abstraction layer to provide counter based monitoring.
 *
 * @author mhawthorne
 */
public abstract class CounterFactory {

    private static CounterFactory INSTANCE;

    /**
     * Pass in a CounterFactory Instance. This must be done to use Zuul as Zuul uses several internal counters
     *
     * @param f a <code>CounterFactory</code> value
     */
    public static final void initialize(CounterFactory f) {
        INSTANCE = f;
    }

    /**
     * return the singleton CounterFactory instance.
     *
     * @return a <code>CounterFactory</code> value
     */
    public static final CounterFactory instance() {
        if (INSTANCE == null)
            throw new IllegalStateException(String.format("%s not initialized", CounterFactory.class.getSimpleName()));
        return INSTANCE;
    }

    /**
     * Increments the counter of the given name
     *
     * @param name a <code>String</code> value
     */
    public abstract void increment(String name);

}
