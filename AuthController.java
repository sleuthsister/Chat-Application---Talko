package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            User user = userService.register(body.get("username"), body.get("password"));
            return ResponseEntity.ok(Map.of("message", "Registered successfully!", "username", user.getUsername()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        Optional<User> user = userService.login(body.get("username"), body.get("password"));
        if (user.isPresent()) {
            session.setAttribute("userId", user.get().getId());
            session.setAttribute("username", user.get().getUsername());
            return ResponseEntity.ok(Map.of("message", "Login successful!", "username", user.get().getUsername()));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid username or password!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out!"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in!"));
        }
        return ResponseEntity.ok(Map.of("username", username));
    }
        @GetMapping("/")
    public void root(HttpSession session, jakarta.servlet.http.HttpServletResponse response) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username != null) {
            response.sendRedirect("/index.html");
        } else {
            response.sendRedirect("/login.html");
        }
    }
}