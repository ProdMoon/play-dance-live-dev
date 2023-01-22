package com.example.manmu.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteData {

    private String type;

    private String sender;

    private String winner;

    private Integer pollLeft;

    private Integer pollRight;

}