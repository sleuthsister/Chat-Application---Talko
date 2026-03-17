package com.example.demo.service;

import com.example.demo.model.FriendRequest;
import com.example.demo.model.User;
import com.example.demo.repository.FriendRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendService {

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    public FriendRequest sendRequest(User sender, User receiver) {
        if (friendRequestRepository.existsBySenderAndReceiver(sender, receiver)) {
            throw new RuntimeException("Friend request already sent!");
        }
        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequest.Status.PENDING);
        return friendRequestRepository.save(request);
    }

    public FriendRequest acceptRequest(Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found!"));
        request.setStatus(FriendRequest.Status.ACCEPTED);
        return friendRequestRepository.save(request);
    }

    public FriendRequest rejectRequest(Long requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found!"));
        request.setStatus(FriendRequest.Status.REJECTED);
        return friendRequestRepository.save(request);
    }

    public List<FriendRequest> getPendingRequests(User user) {
        return friendRequestRepository.findByReceiverAndStatus(user, FriendRequest.Status.PENDING);
    }

    public List<FriendRequest> getFriends(User user) {
        return friendRequestRepository.findBySenderAndStatus(user, FriendRequest.Status.ACCEPTED);
    }

    public boolean areFriends(User user1, User user2) {
        return friendRequestRepository.findBySenderAndReceiver(user1, user2)
                .map(r -> r.getStatus() == FriendRequest.Status.ACCEPTED)
                .orElseGet(() -> friendRequestRepository.findBySenderAndReceiver(user2, user1)
                        .map(r -> r.getStatus() == FriendRequest.Status.ACCEPTED)
                        .orElse(false));
    }
}