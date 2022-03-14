package com.udemy.exmple.websecurity.constant;

public class SecurityConstant {
    public static final long EXPIRATION_TIME = 432000000L;
    public static final String Token_PREFIX = "Bearer";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    public static final String COMPANY_LLC = "COMPANY LLC";
    public static final String COMPANY_ADMIN = "COMPANY ADMIN";
    public static final String AUTHORITIES = "Authorities";
    public static final String FORBIDDEN_MESSAGE = "You need to login to access the company";
    public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page";
    public static final String OPTIONS_HTTP_METHOD = "OPTIONS";
    public static final String[] PUBLIC_URLS = {"/user/login", "/user/register", "/user/resetPassword/**", "/user/image/**" };
//   public static final String[] PUBLIC_URLS = {"/user/login", "/user/register", "/user/resetpassword/**", "/user/image/**};
}
