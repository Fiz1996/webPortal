package com.udemy.exmple.websecurity.service;

import com.udemy.exmple.websecurity.domain.Users;
import com.udemy.exmple.websecurity.exception.domain.EmailExistsException;
import com.udemy.exmple.websecurity.exception.domain.EmailNotFoundException;
import com.udemy.exmple.websecurity.exception.domain.UsernameExistsException;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface UserService {
    Users register(String firstName, String lastName, String username, String email) throws EmailExistsException, UsernameExistsException, MessagingException;

    List<Users> getUsers();

    Users findByUsername(String username);

    Users findUserByEmail(String email);

    Users addNewUsers(String firstName, String lastName, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile image) throws EmailExistsException, UsernameExistsException, IOException;

    Users updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNotLocked, boolean isActive, MultipartFile image) throws EmailExistsException, UsernameExistsException, IOException;

    void delete(long id);
    void resetPassword(String email) throws MessagingException, EmailNotFoundException;

    Users updateProfileImage(String username, MultipartFile profileImage) throws EmailExistsException, UsernameExistsException, IOException;
}
