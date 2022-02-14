package com.udemy.exmple.websecurity.constant;

public class Authority {
    public static final String[] USER_AUTHORITY = {"user:read"};
    public static final String[] HR_AUTHORITY = {"user:read","user:update"};
    public static final String[] MANAGER_AUTHORITY = {"user:read","user:update"};
    public static final String[] ADMIN_AUTHORITY = {"user:read","user:update","user:create"};
    public static final String[] SUPER_ADMIN_AUTHORITY = {"user:read","user:update","user:create","user:update"};
}
