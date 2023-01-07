package com.example.manmu.controller;



import com.example.manmu.repository.LikeRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class LikeController {

    private final LikeRepository likeRepository;

    public LikeController(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @MessageMapping("/like")
    @SendTo("/app/addlike")
    public int addLike(Principal principal) {
        likeRepository.addLike();
        return likeRepository.getLike();
    }

//    @Scheduled(fixedDelay = 1000)
    @SendTo("/topic/getlike")
    public int getLike(Principal principal) {
        return likeRepository.getLike();
    }
}
