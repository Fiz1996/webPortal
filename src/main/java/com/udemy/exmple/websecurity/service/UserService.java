package com.udemy.exmple.websecurity.service;

import com.udemy.exmple.websecurity.domain.Users;
import com.udemy.exmple.websecurity.exception.domain.EmailExistsException;
import com.udemy.exmple.websecurity.exception.domain.UsernameExistsException;

import java.util.List;

public interface UserService {
    Users register(String firstName, String lastName, String username,String email) throws EmailExistsException, UsernameExistsException;

    List<Users> getUsers();

    Users findByUsername(String username);
    Users findUserByEmail(String email);
}
