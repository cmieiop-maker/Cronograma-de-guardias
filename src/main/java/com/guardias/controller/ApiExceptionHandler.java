package com.guardias.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> handle(RuntimeException e) { return Map.of("message", e.getMessage() == null ? "Operación no válida" : e.getMessage()); }
}
