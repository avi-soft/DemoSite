package com.community.api.endpoint.avisoft.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

public class Account {
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        String sessionId = (String) session.getAttribute("sessionId");

        if (sessionId != null) {
            session.removeAttribute("sessionId");
            session.invalidate();
            return ResponseEntity.ok("Logout successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No active session to logout");
        }
    }
}
