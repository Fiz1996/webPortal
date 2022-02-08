package com.udemy.exmple.websecurity.controller;

import com.udemy.exmple.websecurity.domain.Users;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/user")
public class UserController {

    @GetMapping("/home")
    public String showUsers() {
        return "application works";
    }
}
