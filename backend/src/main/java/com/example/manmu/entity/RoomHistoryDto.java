package com.example.manmu.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RoomHistoryDto {
    private String roomId;
    private String winner;
    private List<String> songs;
    private int highestUserNum;

    public RoomHistoryDto(RoomHistory roomHistory) {
        this.roomId = roomHistory.getRoomId();
        this.winner = roomHistory.getWinner();
        this.songs = roomHistory.getSongs();
        this.highestUserNum = roomHistory.getHighestUserNum();
    }

    public RoomHistoryDto(String roomId, String winner, List<String> songs, int highestUserNum) {
        this.roomId = roomId;
        this.winner = winner;
        this.songs = songs;
        this.highestUserNum = highestUserNum;
    }

    public RoomHistory toEntity() {
        return RoomHistory.builder()
                .roomId(roomId)
                .winner(winner)
                .songs(songs)
                .highestUserNum(highestUserNum)
                .build();
    }
}
