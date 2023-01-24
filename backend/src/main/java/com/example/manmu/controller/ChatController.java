package com.example.manmu.controller;


import com.example.manmu.ChatMessage;
import com.example.manmu.Click;
import com.example.manmu.GameSignal;
import com.example.manmu.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate template;
    private final VoteService voteService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        template.convertAndSend("/topic/public", chatMessage);
    }

    @MessageMapping("/chat.sendClick")
    public void sendMessage(@Payload Click click) {
        template.convertAndSend("/topic/public", click);
    }

    @MessageMapping("/chat.sendGameSignal")
    public void sendMessage(@Payload GameSignal gameSignal) {
        template.convertAndSend("/topic/public", gameSignal);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage) {
        template.convertAndSend("/topic/public", chatMessage);
    }


}










