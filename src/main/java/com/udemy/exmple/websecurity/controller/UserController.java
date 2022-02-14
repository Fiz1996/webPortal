package com.udemy.exmple.websecurity.controller;

import com.udemy.exmple.websecurity.domain.UserPrincipal;
import com.udemy.exmple.websecurity.domain.Users;
import com.udemy.exmple.websecurity.exception.domain.EmailExistsException;
import com.udemy.exmple.websecurity.exception.domain.ExceptionHandling;
import com.udemy.exmple.websecurity.exception.domain.UsernameExistsException;
import com.udemy.exmple.websecurity.repository.UserRepository;
import com.udemy.exmple.websecurity.service.UserService;
import com.udemy.exmple.websecurity.utility.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;


import static com.udemy.exmple.websecurity.constant.SecurityConstant.JWT_TOKEN_HEADER;
@Slf4j
@RestController
@RequestMapping(value="/user")
public class UserController extends ExceptionHandling {
    private UserService userService;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @PostMapping("/login")
    public ResponseEntity<Users> login(@RequestBody Users users)   {
//        log.info(String.valueOf(userService.findByUsername(users.getUsername())));
        authenticate(users.getUsername(),users.getPassword());
        Users loginUser = userService.findByUsername(users.getUsername());
        log.info("Username is + " +loginUser);
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);

        return new ResponseEntity<>( loginUser,jwtHeader, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<Users> register(@RequestBody Users users) throws EmailExistsException, UsernameExistsException {
        Users user =  userService.register(users.getFirstName(), users.getLastName(), users.getUsername(), users.getEmail());
        return new ResponseEntity<>( user, HttpStatus.OK);

//        return "application works";

    }


    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER , jwtTokenProvider.generateToken(userPrincipal));
        return headers;

    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));
    }

}
