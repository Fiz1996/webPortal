package com.udemy.exmple.websecurity.exception.domain;

import com.udemy.exmple.websecurity.domain.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.Objects;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestControllerAdvice
public class ExceptionHandling implements ErrorController {
    private static final String ACCOUNT_LOCKED = "Your Account has been locked";
    private static final String METHOD_IS_NOT_ALLOWED = "method is not allowed , Please send a valid '%s' request";
    private static final String ACCOUNT_DISABLED = "Your account has been disabled";
    private static final String EMAIL_EXISTS_EXCEPTION = "This email already exists";
    public static final String USER_NAME_EXISTS_EXCEPTION = "Username already exists";
    private static final String ERROR_PATH = "/error";
    public static final String USERNAME_OR_PASSWORD_IS_INCORRECT = "username or password is incorrect";
    public static final String EMAIL_NOT_FOUND = "email not found";
    private final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandling.class);

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> accountDisabledException() {
        return createHttpResponse(HttpStatus.BAD_REQUEST, ACCOUNT_DISABLED);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> lockedException() {
        return createHttpResponse(UNAUTHORIZED, ACCOUNT_LOCKED);
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<HttpResponse> emailNotFound() {
        return createHttpResponse(HttpStatus.BAD_REQUEST, EMAIL_NOT_FOUND);
    }

    @ExceptionHandler(UsernameExistsException.class)
    public  ResponseEntity<HttpResponse> userNameExists() {
       return createHttpResponse(HttpStatus.BAD_REQUEST,USER_NAME_EXISTS_EXCEPTION);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> badCredentialException() {
        return createHttpResponse(HttpStatus.BAD_REQUEST, USERNAME_OR_PASSWORD_IS_INCORRECT);
    }

    @ExceptionHandler(EmailExistsException.class)
    ResponseEntity<HttpResponse> emailExistsException() {
        return createHttpResponse(HttpStatus.BAD_REQUEST, EMAIL_EXISTS_EXCEPTION);
    }

//    @ExceptionHandler(NoHandlerFoundException.class)
//    public ResponseEntity<HttpResponse> methodNotSupportedException(NoHandlerFoundException exception) {
//        return createHttpResponse(HttpStatus.BAD_REQUEST,"This page was not found");
//    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponse> methodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        HttpMethod supportedMethod = Objects.requireNonNull(exception.getSupportedHttpMethods()).iterator().next();
        return createHttpResponse(HttpStatus.METHOD_NOT_ALLOWED, String.format(METHOD_IS_NOT_ALLOWED, supportedMethod));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> internalServerErrorException(Exception exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus httpStatus, String message) {
        HttpResponse httpResponse = new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase()
                , message.toUpperCase(), new Date());
        return new ResponseEntity<>(httpResponse, httpStatus);
    }


}
