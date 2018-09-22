package com.moguhu.zuul.monitoring;

/**
 * Abstraction layer to provide time-based monitoring.
 *
 * @author mhawthorne
 */
public abstract class TracerFactory {

    private static TracerFactory INSTANCE;

    /**
     * sets a TracerFactory Implementation
     *
     * @param f a <code>TracerFactory</code> value
     */
    public static final void initialize(TracerFactory f) {
        INSTANCE = f;
    }


    /**
     * Returns the singleton TracerFactory
     *
     * @return a <code>TracerFactory</code> value
     */
    public static final TracerFactory instance() {
        if (INSTANCE == null)
            throw new IllegalStateException(String.format("%s not initialized", TracerFactory.class.getSimpleName()));
        return INSTANCE;
    }

    public abstract Tracer startMicroTracer(String name);

}
