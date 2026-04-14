package com.marmot.qilu.common.context;

public class UserContext {

    private static final ThreadLocal<String> UUID_HOLDER = new ThreadLocal<>();

    private UserContext() { }

    public static void setUuid(String uuid) {
        UUID_HOLDER.set(uuid);
    }

    public static String getUuid() {
        return UUID_HOLDER.get();
    }

    public static void clear() {
        UUID_HOLDER.remove();
    }
}
