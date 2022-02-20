package com.udemy.exmple.websecurity.service.impl;

import com.udemy.exmple.websecurity.domain.UserPrincipal;
import com.udemy.exmple.websecurity.domain.Users;
import com.udemy.exmple.websecurity.enumeration.Role;
import com.udemy.exmple.websecurity.exception.domain.EmailExistsException;
import com.udemy.exmple.websecurity.exception.domain.EmailNotFoundException;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import javax.persistence.AssociationOverride;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static com.udemy.exmple.websecurity.constant.FileConstant.*;
import static com.udemy.exmple.websecurity.constant.UserImplConstant.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private LoginAttemptImpl loginAttemptService;
    private EmailServiceImpl emailService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, LoginAttemptImpl loginAttemptService, EmailServiceImpl emailService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.emailService = emailService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findUsersByUsername(username);
        if (user == null) {
            LOGGER.error("User not found by username " + username);
            throw new UsernameNotFoundException("User not found by username " + username);
        } else {
            validateLoginAttempt(user);
            user.setLastLoginDateDisplay(user.getLastLoginDate());
            user.setLastLoginDate(new Date());
            userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            LOGGER.info("Returning found user by username " + username);
            return userPrincipal;
        }
    }

    private void validateLoginAttempt(Users user) {
        if (user.isNotLocked()) {
            if (loginAttemptService.hasExceededMaxAttempts(user.getUsername())) {
                user.setNotLocked(false);
            } else {
                user.setNotLocked(true);
            }
        } else {
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    @Override
    public Users register(String firstName, String lastName, String username, String email) throws EmailExistsException, UsernameExistsException, MessagingException {
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
        user.setProfileImageUrl(getTemporaryProfileImageUrl(user.getProfileImageUrl()));
        userRepository.save(user);
        emailService.sendNewPasswordEmail(firstName, lastName, password, email);
        return user;
    }

    private String getTemporaryProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE + username).toString();
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

    @Override
    public Users addNewUsers(String firstName, String lastName, String username, String email, String role, boolean isNotLocked, boolean isActive, MultipartFile image) throws EmailExistsException, UsernameExistsException, IOException {
        validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);
        Users user = new Users();
        String password = generatePassword();
        user.setUserId(generateUserId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setJoinDate(new Date());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodePassword(password));
        user.setActive(isActive);
        user.setNotLocked(isNotLocked);
        user.setRole(getRoleEnumName(role).name());
        user.setAuthorities(getRoleEnumName(role).getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        userRepository.save(user);
        saveProfileImage(user, image);
        return user;
    }


    @Override
    public Users updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername, String newEmail, String role, boolean isNotLocked, boolean isActive, MultipartFile image) throws EmailExistsException, UsernameExistsException, IOException {
        Users currentUser = validateNewUsernameAndEmail(StringUtils.EMPTY, newUsername, newEmail);
        if (currentUser != null) {
            currentUser.setFirstName(newFirstName);
        }
        if (currentUser != null) {
            currentUser.setLastName(newLastName);
        }
        if (currentUser != null) {
            currentUser.setJoinDate(new Date());
        }
        if (currentUser != null) {
            currentUser.setUsername(newUsername);
        }
        if (currentUser != null) {
            currentUser.setEmail(newEmail);
        }
        if (currentUser != null) {
            currentUser.setActive(isActive);
        }
        if (currentUser != null) {
            currentUser.setNotLocked(isNotLocked);
        }
        if (currentUser != null) {
            currentUser.setRole(getRoleEnumName(role).name());
        }
        if (currentUser != null) {
            currentUser.setProfileImageUrl(getTemporaryProfileImageUrl(currentUsername));
        }
        if (currentUser != null) {
            currentUser.setAuthorities(getRoleEnumName(role).getAuthorities());
        }
        if (currentUser != null) {
            userRepository.save(currentUser);
        }
        saveProfileImage(currentUser, image);
        return currentUser;
    }

    @Override
    public void delete(long id) {
        userRepository.deleteById(id);

    }

    @Override
    public void resetPassword(String email) throws MessagingException, EmailNotFoundException {
        Users user = userRepository.findUsersByEmail(email);
        if (user == null) {
            throw new EmailNotFoundException(EMAIL_NOT_FOUND + email);
        }
        String password = generatePassword();
        user.setPassword(encodePassword(password));
        userRepository.save(user);
        emailService.sendNewPasswordEmail(user.getFirstName(), user.getLastName(), password, user.getEmail());
    }

    @Override
    public Users updateProfileImage(String username, MultipartFile profileImage) throws EmailExistsException, UsernameExistsException, IOException {
        Users user = validateNewUsernameAndEmail(username, null, null);
        saveProfileImage(user, profileImage);
        return null;
    }

    private void saveProfileImage(Users user, MultipartFile profileImage) throws IOException {
        if (profileImage.isEmpty()) {
            Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();
            if (!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
                LOGGER.info(DIRECTORY_CREATED);
            }
            Files.deleteIfExists(Paths.get(userFolder + user.getUsername()+DOT,JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(),userFolder.resolve(user.getUsername() +DOT +JPG_EXTENSION) ,REPLACE_EXISTING);
            user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
            userRepository.save(user);
            LOGGER.info(FILE_SAVED_IN_FILE_SYSTEM +profileImage.getOriginalFilename());

        }
    }

    private String setProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + username +"\""
        +username+DOT+JPG_EXTENSION).toUriString();
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }
}
