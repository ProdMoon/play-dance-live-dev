package com.example.manmu.controller;


import com.example.manmu.ChatMessage;
import com.example.manmu.config.auth.dto.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class ChatController {

//    private final HttpSession httpSession;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
//        SessionUser user = (SessionUser) httpSession.getAttribute("user");
//        chatMessage.setSender(user.getName());
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/{roomId}")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor, HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", user.getName());
        return chatMessage;
    }

}


