package com.udemy.exmple.websecurity.controller;

import com.udemy.exmple.websecurity.domain.HttpResponse;
import com.udemy.exmple.websecurity.domain.UserPrincipal;
import com.udemy.exmple.websecurity.domain.Users;
import com.udemy.exmple.websecurity.exception.domain.EmailExistsException;
import com.udemy.exmple.websecurity.exception.domain.EmailNotFoundException;
import com.udemy.exmple.websecurity.exception.domain.ExceptionHandling;
import com.udemy.exmple.websecurity.exception.domain.UsernameExistsException;
import com.udemy.exmple.websecurity.service.UserService;
import com.udemy.exmple.websecurity.utility.JWTTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.mail.MessagingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static com.udemy.exmple.websecurity.constant.FileConstant.*;
import static com.udemy.exmple.websecurity.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/user")
public class UserController extends ExceptionHandling {
    public static final String EMAIL_RESET = "An email with a new password sent to:";
    public static final String USER_DELETED_SUCCESSFULLY = "User deleted Successfully";
    private UserService userService;
    private AuthenticationManager authenticationManager;
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JWTTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @PostMapping("/login")
    public ResponseEntity<Users> login(@RequestBody Users users) {
//        log.info(String.valueOf(userService.findByUsername(users.getUsername())));
        authenticate(users.getUsername(), users.getPassword());
        Users loginUser = userService.findByUsername(users.getUsername());
        log.info("Username is + " + loginUser);
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);

        return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<Users> register(@RequestBody Users users) throws EmailExistsException, UsernameExistsException, MessagingException {
        Users user = userService.register(users.getFirstName(), users.getLastName(), users.getUsername(), users.getEmail());
        return new ResponseEntity<>(user, HttpStatus.OK);

//        return "application works";

    }

    @PostMapping("/add")
    public ResponseEntity<Users> addNewUser(@RequestParam("firstName") String firstName,
                                            @RequestParam("lastName") String lastName,
                                            @RequestParam("username") String username,
                                            @RequestParam("email") String email,
                                            @RequestParam("role") String role,
                                            @RequestParam("isActive") String isActive,
                                            @RequestParam("isNotLocked") String isNotLocked,
                                            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UsernameExistsException, IOException, EmailExistsException {
        Users newUser = userService.addNewUsers(firstName, lastName, username, email, role
                , Boolean.parseBoolean(isActive), Boolean.parseBoolean(isNotLocked), profileImage);
        return new ResponseEntity<>(newUser, HttpStatus.OK);

    }

    @PostMapping("/update")
    public ResponseEntity<Users> updateUser(@RequestParam("currentUsername") String currentUsername,
                                            @RequestParam("firstName") String firstName,
                                            @RequestParam("lastName") String lastName,
                                            @RequestParam("username") String username,
                                            @RequestParam("email") String email,
                                            @RequestParam("role") String role,
                                            @RequestParam("isActive") String isActive,
                                            @RequestParam("isNotLocked") String isNotLocked,
                                            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws UsernameExistsException, IOException, EmailExistsException {
        Users updatedUser = userService.updateUser(currentUsername, firstName, lastName, username, email, role
                , Boolean.parseBoolean(isActive), Boolean.parseBoolean(isNotLocked), profileImage);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);

    }


    @GetMapping("/find/{username}")
    public ResponseEntity<Users> getUser(@PathVariable("username") String username) {
        Users user = userService.findByUsername(username);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    ;

    @GetMapping("/resetPassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) throws MessagingException, EmailNotFoundException {
        userService.resetPassword(email);
        return response(HttpStatus.OK, EMAIL_RESET + email);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("id") long id) {
        userService.delete(id);
        return response(HttpStatus.NO_CONTENT, USER_DELETED_SUCCESSFULLY);
    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<Users> updateProfileImage(@RequestParam("username") String username,
                                                    @RequestParam(value = "profileImage", required = true) MultipartFile profileImage) throws UsernameExistsException, IOException, EmailExistsException {
        Users user = userService.updateProfileImage(username, profileImage);
        return new ResponseEntity<>(user, HttpStatus.OK);

    }

    @GetMapping(path = "/image/{username}/{filename}", produces = IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("fileName") String filename) throws IOException {
        return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_DASH + filename));
    }

    @GetMapping(path = "/image/profile/{username}", produces = IMAGE_JPEG_VALUE)
    public byte[] getTempProfileImage(@PathVariable("username") String username) throws IOException {
        URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL + username);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = url.openStream()) {
            int bytesRead;
            byte[] chunk = new byte[1024];
            while ((bytesRead = inputStream.read(chunk)) > 0) {
                byteArrayOutputStream.write(chunk, 0, bytesRead);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {

        return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(), message.toUpperCase(), new Date()), httpStatus);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Users>> getAllUsers() {
        List<Users> users = userService.getUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    ;

    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
        return headers;

    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

}
