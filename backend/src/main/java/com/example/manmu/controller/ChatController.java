package com.example.manmu.controller;


import com.example.manmu.ChatMessage;
import com.example.manmu.GameSignal;
import com.example.manmu.Click;
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

    @MessageMapping("/chat.sendClick")
    public void sendMessage(@Payload Click click) {
        template.convertAndSend("/topic/" + click.getRoomId(), click);
    }

    @MessageMapping("/chat.sendGameSignal")
    public void sendMessage(@Payload GameSignal gameSignal) {
        template.convertAndSend("/topic/" + gameSignal.getRoomId(), gameSignal);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage) {
        template.convertAndSend("/topic/" + chatMessage.getRoomId(), chatMessage);
    }

    @MessageMapping("/chat.vote")
    public void Message(@Payload ChatVote chatVote) {
        String type = chatVote.getType();
        String sender = chatVote.getSender();
        String roomId = chatVote.getRoomId();
        String winner = chatVote.getWinner();
        Integer poll = chatVote.getPoll();
        Integer currentRound = chatVote.getCurrentRound();

        // voteService.setMatchInfo(roomId, "winner", winner);
        // voteService.setMatchInfo(roomId, "poll", poll);
        // voteService.setMatchInfo(roomId, "currentRound", currentRound);

        template.convertAndSend("/topic/" + chatVote.getRoomId(), chatVote);
        }
    }










