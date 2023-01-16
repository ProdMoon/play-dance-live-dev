package com.example.manmu.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RoomDto {

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
        this.roomOwner = room.getRoomOwner();
    }

    @Builder
    public RoomDto(String roomId, List<String> users, List songs, Long round1, Long round2, Long winner, String prev, String next, boolean isEmpty, String roomOwner) {
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
                .roomOwner(roomOwner)
                .build();
    }
}
