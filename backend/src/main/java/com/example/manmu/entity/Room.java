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
    String roomId;
    List users;
    List songs;
    Long round1;
    Long round2;
    Long winner;
    Room prev;
    Room next;
    boolean isEmpty;

    @Builder
    public Room(String roomId, List users, List songs, Long round1, Long round2, Long winner, Room prev, Room next, boolean isEmpty) {
        this.roomId = roomId;
        this.users = users;
        this.songs = songs;
        this.round1 = round1;
        this.round2 = round2;
        this.winner = winner;
        this.prev = prev;
        this.next = next;
        this.isEmpty = isEmpty;
    }
}
