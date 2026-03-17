package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.service.FriendService;
import com.example.demo.service.MessageService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@RestController
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/api/messages/{friendUsername}")
    public ResponseEntity<?> getConversation(@PathVariable String friendUsername, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return ResponseEntity.status(401).body(Map.of("error", "Not logged in!"));

        User user = userService.findByUsername(username).orElseThrow();
        User friend = userService.findByUsername(friendUsername).orElseThrow();

        if (!friendService.areFriends(user, friend)) {
            return ResponseEntity.status(403).body(Map.of("error", "You are not friends!"));
        }

        List<Message> messages = messageService.getConversation(user, friend);
        return ResponseEntity.ok(messages);
    }

    // WebSocket - send message
    @MessageMapping("/chat")
    public void sendMessage(@Payload Map<String, String> payload,
                            SimpMessageHeaderAccessor headerAccessor) {
        // Get username from WebSocket session attributes
        String senderUsername = (String) headerAccessor.getSessionAttributes().get("username");
        if (senderUsername == null) return;

        String receiverUsername = payload.get("receiver");
        String content = payload.get("content");

        User sender = userService.findByUsername(senderUsername).orElse(null);
        User receiver = userService.findByUsername(receiverUsername).orElse(null);
        if (sender == null || receiver == null) return;

        if (!friendService.areFriends(sender, receiver)) return;

        messageService.sendMessage(sender, receiver, content);

        Map<String, String> response = Map.of(
            "sender", senderUsername,
            "content", content
        );

        messagingTemplate.convertAndSendToUser(receiverUsername, "/queue/messages", response);
        messagingTemplate.convertAndSendToUser(senderUsername, "/queue/messages", response);
    }

    // WebSocket - typing indicator
    @MessageMapping("/typing")
    public void typing(@Payload Map<String, String> payload,
                       SimpMessageHeaderAccessor headerAccessor) {
        String senderUsername = (String) headerAccessor.getSessionAttributes().get("username");
        if (senderUsername == null) return;

        String receiver = payload.get("receiver");
        messagingTemplate.convertAndSendToUser(receiver, "/queue/typing",
            Map.of("username", senderUsername, "typing", payload.get("typing")));
    }
}