package com.moguhu.zuul;

/**
 * Metadata about the Zuul instance/ application name and "stack"
 */
public class ZuulApplicationInfo {
    private static String applicationName;
    private static String stack;

    public static String getApplicationName() {
        return applicationName;
    }

    public static void setApplicationName(String applicationName) {
        ZuulApplicationInfo.applicationName = applicationName;
    }

    public static String getStack() {
        return stack;
    }

    public static void setStack(String stack) {
        ZuulApplicationInfo.stack = stack;
    }
}
