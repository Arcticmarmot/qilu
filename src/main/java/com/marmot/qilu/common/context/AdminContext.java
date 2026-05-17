package com.marmot.qilu.common.context;

public class AdminContext {

    private static final ThreadLocal<String> UUID_HOLDER = new ThreadLocal<>();

    private AdminContext() {}

    public static void setUuid(String uuid) {
        UUID_HOLDER.set(uuid);
    }

    public static String getUuid() {
        return UUID_HOLDER.get();
    }

    public static String requireUuid() {
        String uuid = AdminContext.getUuid();
        if(uuid == null) {
            throw new RuntimeException("admin user not logged in.");
        }
        return uuid;
    }

    public static void clear() {
        UUID_HOLDER.remove();
    }
}
