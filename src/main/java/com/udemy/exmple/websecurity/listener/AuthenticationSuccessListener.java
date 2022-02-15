package com.udemy.exmple.websecurity.listener;

import com.udemy.exmple.websecurity.domain.UserPrincipal;
import com.udemy.exmple.websecurity.domain.Users;
import com.udemy.exmple.websecurity.service.impl.LoginAttemptImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener {
    private LoginAttemptImpl loginAttemptService;

    @Autowired
    public AuthenticationSuccessListener(LoginAttemptImpl loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if(principal instanceof UserPrincipal) {
            UserPrincipal user = (UserPrincipal) event.getAuthentication().getPrincipal();
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }
}
