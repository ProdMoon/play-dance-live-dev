package com.example.manmu.controller;


import com.example.manmu.ChatMessage;
import com.example.manmu.GameSignal;
import com.example.manmu.Click;
import com.example.manmu.config.auth.dto.SessionUser;
import lombok.RequiredArgsConstructor;
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
    public void addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor, HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", user.getName());
        template.convertAndSend("/topic/" + chatMessage.getRoomId(), chatMessage);
    }

}


