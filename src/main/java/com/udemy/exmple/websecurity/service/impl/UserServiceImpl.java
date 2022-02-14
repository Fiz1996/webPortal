package com.udemy.exmple.websecurity.service.impl;

import com.udemy.exmple.websecurity.domain.UserPrincipal;
import com.udemy.exmple.websecurity.domain.Users;
import com.udemy.exmple.websecurity.enumeration.Role;
import com.udemy.exmple.websecurity.exception.domain.EmailExistsException;
import com.udemy.exmple.websecurity.exception.domain.UsernameExistsException;
import com.udemy.exmple.websecurity.repository.UserRepository;
import com.udemy.exmple.websecurity.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static com.udemy.exmple.websecurity.constant.UserImplConstant.*;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findUsersByUsername(username);
        if (user == null) {
            LOGGER.error("User not found by username " + username);
            throw new UsernameNotFoundException("User not found by username " + username);
        } else {
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            LOGGER.info("Returning found user by username " + username);
            return userPrincipal;
        }
    }

    @Override
    public Users register(String firstName, String lastName, String username, String email) throws EmailExistsException, UsernameExistsException {
        validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
        Users user = new Users();
        user.setUserId(generateUserId());
        String password = generatePassword();
        String encodedPassword = encodePassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setPassword(encodedPassword);
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(Role.ROLE_USER.name());
        user.setAuthorities(Role.ROLE_USER.getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl());
        userRepository.save(user);
        LOGGER.info("New user password is =   " + password);
        return user;
    }

    private String getTemporaryProfileImageUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE).toString();
    }

    private String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private Users validateNewUsernameAndEmail(String currentUserName, String newUsername, String newEmail) throws UsernameExistsException, EmailExistsException {
        Users userByNewUsername = findByUsername(newUsername);
        Users userByNewEmail = findUserByEmail(newEmail);
        if (StringUtils.isNotEmpty(currentUserName)) {
            Users currentUser = findByUsername(currentUserName);
            if (currentUser == null) {
                throw new UsernameNotFoundException("No user found by username" + currentUserName);
            }
            if (userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())) {
                throw new UsernameExistsException(USERNAME_ALREADY_EXISTS);
            }
            if (userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistsException(EMAIL_ALREADY_EXISTS);
            }
            return currentUser;
        } else {
            if (userByNewUsername != null) {
                throw new UsernameExistsException(USERNAME_ALREADY_EXISTS);
            }
            if (userByNewEmail != null) {
                throw new EmailExistsException(EMAIL_ALREADY_EXISTS);
            }

            return null;
        }
    }

    @Override
    public List<Users> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public Users findByUsername(String username) {
        return userRepository.findUsersByUsername(username);
    }

    @Override
    public Users findUserByEmail(String email) {
        return userRepository.findUsersByEmail(email);
    }
}
