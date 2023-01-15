package com.example.manmu.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatVote {

    private String roomId;

    //  음수이면 왼쪽 승, 양수이면 오른쪽 승, 0이면 무승부
    private Integer result;

    private Boolean isEnd;

    private Integer poll;

}