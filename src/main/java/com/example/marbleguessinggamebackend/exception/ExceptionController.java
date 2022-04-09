package com.example.marbleguessinggamebackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionController {

  // Called whenever GameException is thrown
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(GameException.class)
  public ResponseEntity<Error> exception(GameException e) {
    // Create an error object with the message contianing details about the
    // exception
    Error error = new Error(HttpStatus.BAD_REQUEST, e.getMessage());

    return ResponseEntity.badRequest().body(error);
  }
}
