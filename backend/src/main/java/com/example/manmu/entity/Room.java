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
    private Integer pubCount = 0;
    private Integer subCount = 0;
    private RoomState state;
    private String sessionId;
    private List<Song> songs = synchronizedList(new ArrayList<>());
    private List<User> subscribers = synchronizedList(new ArrayList<>());
    private List<User> publishers = synchronizedList(new ArrayList<>());


    @Builder
    public Room(String roomId, Integer pubCount, Integer subCount, RoomState state, String sessionId, List<Song> songs, List<User> subscribers, List<User> publishers) {
        this.roomId = roomId;
        this.pubCount = pubCount;
        this.subCount = subCount;
        this.state = state;
        this.sessionId = sessionId;
        this.songs = songs;
        this.subscribers = subscribers;
        this.publishers = publishers;
    }

}
