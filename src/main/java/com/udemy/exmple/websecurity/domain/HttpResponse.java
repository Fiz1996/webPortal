package com.udemy.exmple.websecurity.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.TimeZone;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HttpResponse {
    private int httpStatusCode;
    private HttpStatus httpStatus;
    private String message;
    private String code;
    @Setter(value = AccessLevel.NONE)
    @JsonFormat(shape = JsonFormat.Shape.STRING ,pattern = "MM-dd-yyyy hh:mm:ss", timezone ="Asia/Riyadh")
    private Date timeStamp= new Date();
}
