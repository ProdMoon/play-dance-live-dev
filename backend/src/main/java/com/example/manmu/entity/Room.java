package com.example.manmu.entity;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import java.io.Serializable;
import java.util.List;

@RedisHash("Room")
@Getter
@Setter
@NoArgsConstructor
public class Room implements Serializable {
    private String roomId;
    private List<String> users;
    private List songs;
    private Long round1;
    private Long round2;
    private Long winner;
    private String prev;
    private String next;
    private boolean isEmpty;
    private String roomOwner;


    @Builder
    public Room(String roomId, List<String> users, List songs, Long round1, Long round2, Long winner, String prev, String next, boolean isEmpty, String roomOwner) {
        this.roomId = roomId;
        this.users = users;
        this.songs = songs;
        this.round1 = round1;
        this.round2 = round2;
        this.winner = winner;
        this.prev = prev;
        this.next = next;
        this.isEmpty = isEmpty;
        this.roomOwner = roomOwner;
    }

    public Room update(String roomId, List<String> users, List songs, Long round1, Long round2, Long winner, String prev, String next, boolean isEmpty, String roomOwner) {
        this.roomId = roomId;
        this.users = users;
        this.songs = songs;
        this.round1 = round1;
        this.round2 = round2;
        this.winner = winner;
        this.prev = prev;
        this.next = next;
        this.isEmpty = isEmpty;
        this.roomOwner = roomOwner;
        return this;
    }

}
