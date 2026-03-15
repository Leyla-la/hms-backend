package com.hms.appointment.utility;

import com.hms.appointment.exception.HmsException;
import com.hms.appointment.utility.ErrorInfo;
import jakarta.validation.ConstraintViolationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExceptionControllerAdvice {
    Environment env;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> exceptionHandler(Exception e) {
        ErrorInfo error = new ErrorInfo("Some error occurred", HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HmsException.class)
    public ResponseEntity<ErrorInfo> hmsExceptionHandler(HmsException e) {
        ErrorInfo error = new ErrorInfo(env.getProperty(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorInfo> validationExceptionHandler(Exception e) {
        String errorMsg;
        if (e instanceof MethodArgumentNotValidException manve) {
            errorMsg = manve.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(","));
        } else {
            ConstraintViolationException cve = (ConstraintViolationException) e;
            errorMsg = cve.getConstraintViolations().stream().map(cv -> cv.getMessage()).collect(Collectors.joining(","));
        }
            ErrorInfo error = new ErrorInfo(errorMsg, HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

    }


}
