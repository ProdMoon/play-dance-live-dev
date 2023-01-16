package com.example.manmu.controller;


import com.example.manmu.ChatMessage;
import com.example.manmu.config.auth.dto.SessionUser;
import com.example.manmu.entity.ChatVote;
import com.example.manmu.entity.Room;
import com.example.manmu.repository.PlayingRoomRepository;
import com.example.manmu.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate template;
    private final VoteService voteService;

    private final PlayingRoomRepository playingRoomRepository;

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
            // Room entity는 controller 단에서 제어 되면 안됨 -> service에서 제어해주어야 함.
            Room room = playingRoomRepository.findById(roomId).orElse(null);
            if (userCount == room.getUsers().size())  {
                Integer result = voteService.getMatchResult(roomId);
                chatVote.setResult(result);
            }
        }
    }
}


