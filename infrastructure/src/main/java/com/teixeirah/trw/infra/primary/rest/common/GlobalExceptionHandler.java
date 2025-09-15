package com.teixeirah.trw.infra.primary.rest.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
    return response(HttpStatus.BAD_REQUEST, ex, req);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiError> handleConflict(IllegalStateException ex, HttpServletRequest req) {
    return response(HttpStatus.CONFLICT, ex, req);
  }

  @ExceptionHandler({NoSuchElementException.class})
  public ResponseEntity<ApiError> handleNotFound(RuntimeException ex, HttpServletRequest req) {
    return response(HttpStatus.NOT_FOUND, ex, req);
  }

  @ExceptionHandler(Throwable.class)
  public ResponseEntity<ApiError> handleFallback(Throwable ex, HttpServletRequest req) {
    return response(HttpStatus.INTERNAL_SERVER_ERROR, ex, req);
  }

  private ResponseEntity<ApiError> response(HttpStatus status, Throwable ex, HttpServletRequest req) {
    var body = new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), ex.getMessage(), req.getRequestURI(), null);
    return ResponseEntity.status(status).body(body);
  }
}


