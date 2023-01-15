package com.example.manmu.controller;


import com.example.manmu.ChatMessage;
import com.example.manmu.config.auth.dto.SessionUser;
import com.example.manmu.entity.ChatVote;
import com.example.manmu.entity.Room;
import com.example.manmu.repository.TestRepository;
import com.example.manmu.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate template;
    private final VoteService voteService;

    private final TestRepository testRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        template.convertAndSend("/topic/" + chatMessage.getRoomId(), chatMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor, HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", user.getName());
        template.convertAndSend("/topic/" + chatMessage.getRoomId(), chatMessage);
    }

    @MessageMapping("/chat.vote")
    public void Message(@Payload ChatVote chatVote) {
        String roomId = chatVote.getRoomId();
        Integer poll = chatVote.getPoll();
        if (chatVote.getIsEnd() == true) {
            Integer result = voteService.getMatchResult(roomId);
            chatVote.setResult(result);
            template.convertAndSend("/topic/" + chatVote.getRoomId(), chatVote);}
        else {
            voteService.setVoteCount(roomId, poll);
            Integer userCount = voteService.setUserCount(roomId);
            Room room = testRepository.findById(roomId);

            if (userCount == room.getUserCount() ) {
                Integer result = voteService.getMatchResult(roomId);
                chatVote.setResult(result);
            }
        }
    }
}


