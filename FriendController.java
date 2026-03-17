package com.example.demo.controller;

import com.example.demo.model.FriendRequest;
import com.example.demo.model.User;
import com.example.demo.repository.FriendRequestRepository;
import com.example.demo.service.FriendService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestBody Map<String, String> body, HttpSession session) {
        String senderUsername = (String) session.getAttribute("username");
        if (senderUsername == null) return ResponseEntity.status(401).body(Map.of("error", "Not logged in!"));

        User sender = userService.findByUsername(senderUsername).orElseThrow();
        User receiver = userService.findByUsername(body.get("username"))
                .orElseThrow(() -> new RuntimeException("User not found!"));

        try {
            friendService.sendRequest(sender, receiver);
            return ResponseEntity.ok(Map.of("message", "Friend request sent!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<?> acceptRequest(@PathVariable Long requestId, HttpSession session) {
        if (session.getAttribute("username") == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in!"));
        friendService.acceptRequest(requestId);
        return ResponseEntity.ok(Map.of("message", "Friend request accepted!"));
    }

    @PostMapping("/reject/{requestId}")
    public ResponseEntity<?> rejectRequest(@PathVariable Long requestId, HttpSession session) {
        if (session.getAttribute("username") == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in!"));
        friendService.rejectRequest(requestId);
        return ResponseEntity.ok(Map.of("message", "Friend request rejected!"));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPending(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return ResponseEntity.status(401).body(Map.of("error", "Not logged in!"));
        User user = userService.findByUsername(username).orElseThrow();
        List<FriendRequest> pending = friendService.getPendingRequests(user);
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/sent")
    public ResponseEntity<?> getSentRequests(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return ResponseEntity.status(401).body(Map.of("error", "Not logged in!"));
        User user = userService.findByUsername(username).orElseThrow();
        List<FriendRequest> sent = friendRequestRepository.findBySender(user);
        return ResponseEntity.ok(sent);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getFriends(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return ResponseEntity.status(401).body(Map.of("error", "Not logged in!"));
        User user = userService.findByUsername(username).orElseThrow();
        List<FriendRequest> friends = friendService.getFriends(user);
        return ResponseEntity.ok(friends);
    }
}