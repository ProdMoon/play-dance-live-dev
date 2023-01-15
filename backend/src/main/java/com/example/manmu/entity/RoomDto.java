package com.example.manmu.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.synchronizedList;

@Getter
@NoArgsConstructor
public class RoomDto {

    private String roomId;
    private List users;
    private List songs;
    private Long round1;
    private Long round2;
    private Long winner;
    private Room prev;
    private Room next;
    private boolean isEmpty;

    public RoomDto(Room room) {
        this.roomId = room.getRoomId();
        this.users = room.getUsers();
        this.songs = room.getSongs();
        this.round1 = room.getRound1();
        this.round2 = room.getRound2();
        this.winner = room.getWinner();
        this.prev = room.getPrev();
        this.next = room.getNext();
        this.isEmpty = room.isEmpty();
    }

    @Builder
    public RoomDto(String roomId, List users, List songs, Long round1, Long round2, Long winner, Room prev, Room next, boolean isEmpty) {
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

    public Room toEntity() {
        return Room.builder()
                .roomId(roomId)
                .users(users)
                .songs(songs)
                .round1(round1)
                .round2(round2)
                .winner(winner)
                .prev(prev)
                .next(next)
                .isEmpty(isEmpty)
                .build();
    }

    public void update() {

    }
}
