package com.udemy.exmple.websecurity.enumeration;

import com.udemy.exmple.websecurity.constant.Authority;

public enum Role {
    ROLE_USER(Authority.USER_AUTHORITY),
    ROLE_HR(Authority.HR_AUTHORITY),
    ROLE_MANAGER(Authority.MANAGER_AUTHORITY),
    ROLE_ADMIN(Authority.ADMIN_AUTHORITY),
    ROLE_SUPER(Authority.SUPER_ADMIN_AUTHORITY);

    private String[] authorities;

    Role(String... authorities) {
        this.authorities = authorities;
    }

    public String [] getAuthorities() {
        return authorities;
    }
}
