package com.example.manmu.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class RoomHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "roomId", nullable = false)
    private String roomId;
    @Column(name = "winner", nullable = false)
    private String winner;
    @ElementCollection
    @Column(name = "songs", nullable = false)
    private List<String> songs;
    @Column(name = "highestUserNum", nullable = false)
    private int highestUserNum;

    @Builder
    public RoomHistory(String roomId, String winner, List<String> songs, int highestUserNum) {
        this.roomId = roomId;
        this.winner = winner;
        this.songs = songs;
        this.highestUserNum = highestUserNum;
    }
}
