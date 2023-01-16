package com.example.manmu.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatVote {

    private String type;

    private String sender;

    private String roomId;

    private String winner;

    private Integer poll;

    private Integer currentRound;

}