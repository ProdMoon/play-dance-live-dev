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
    transient String roomId;
    transient List<String> users;
    transient List<Song> songs;
    transient Long round1;
    transient Long round2;
    transient Long winner;
    transient String prev;
    transient String next;
    transient boolean isEmpty;
    transient String roomOwner;


    @Builder
    public Room(String roomId, List<String> users, List<Song> songs, Long round1, Long round2, Long winner, String prev, String next, boolean isEmpty, String roomOwner) {
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
}
