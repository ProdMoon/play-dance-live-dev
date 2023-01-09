package com.example.manmu.entity;

import com.example.manmu.RoomState;
import io.openvidu.java.client.Session;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.synchronizedList;

@Getter
@Setter
@NoArgsConstructor
public class Room {
    private String roomId;
    private Integer userCount;
    private RoomState state;
    private List<Song> songs = synchronizedList(new ArrayList<>());
    private List<User> subscribers = synchronizedList(new ArrayList<>());
    private List<User> publishers = synchronizedList(new ArrayList<>());


    @Builder
    public Room(String roomId, Integer userCount ,RoomState state, List<Song> songs) {
        this.roomId = roomId;
        this.userCount = userCount;
        this.state = state;
        this.songs = songs;
    }

}
