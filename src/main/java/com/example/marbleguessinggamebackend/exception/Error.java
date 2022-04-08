package com.example.marbleguessinggamebackend.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class Error {
  private HttpStatus status;
  private Instant timestamp;
  private String message;

  private Error() {
    timestamp = Instant.now();
  }

  public Error(HttpStatus status, String message) {
    this();
    this.status = status;
    this.message = message;
  }
}
