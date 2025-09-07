package com.gcm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/{serviceId}")
    public ResponseEntity<String> fallback(@PathVariable String serviceId) {
        String message = String.format("Service [%s] is temporarily unavailable. Please try again later.", serviceId);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(message);
    }
}