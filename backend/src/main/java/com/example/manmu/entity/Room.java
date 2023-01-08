package com.example.manmu.entity;

import com.example.manmu.RoomState;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Room {
    private String roomId;
    private List<Long> players;
    private RoomState state;
    private List<Song> songs;

}
