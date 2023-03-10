package com.example.manmu.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RoomDto {
    private List<String> viewers;
    private List<UserDto> players;
    private List<UserDto> waiters;
    private List<String> playSongs;
    private List<RankingDto> rankingList;
    private UserDto currentChampion;
    private UserDto currentChallenger;
    private String currentDancerConnectionId;

    @Builder
    public RoomDto(List<String> viewers, List<UserDto> players, List<UserDto> waiters, List<String> playSongs, List<RankingDto> rankingList, UserDto currentChampion, UserDto currentChallenger, String currentDancerConnectionId) {
        this.viewers = viewers;
        this.players = players;
        this.waiters = waiters;
        this.playSongs = playSongs;
        this.rankingList = rankingList;
        this.currentChampion = currentChampion;
        this.currentChallenger = currentChallenger;
        this.currentDancerConnectionId = currentDancerConnectionId;
    }

    public RoomDto(Room room) {
        this.viewers = room.getViewers();
        this.players = room.getPlayers();
        this.waiters = room.getWaiters();
        this.playSongs = room.getPlaySongs();
        this.rankingList = room.getRankingList();
        this.currentChampion = room.getCurrentChampion();
        this.currentChallenger = room.getCurrentChallenger();
        this.currentDancerConnectionId = room.getCurrentDancerConnectionId();
    }

    public Room toEntity() {
        return Room.builder()
                .viewers(viewers)
                .players(players)
                .waiters(waiters)
                .playSongs(playSongs)
                .rankingList(rankingList)
                .currentChampion(currentChampion)
                .currentChallenger(currentChallenger)
                .currentDancerConnectionId(currentDancerConnectionId)
                .build();
    }
}
