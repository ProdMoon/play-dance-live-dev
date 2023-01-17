package com.example.manmu.entity;

import com.example.manmu.domain.user.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer songId;
    @Column
    private String songName;
    @Column
    private String singer;


    @Builder
    public Song(Integer songId, String songName, String singer) {
        this.songId = songId;
        this.songName = songName;
        this.singer = singer;
    }

    public Song update(Integer songId, String songName, String singer) {
        this.songId = songId;
        this.songName = songName;
        this.singer = singer;
        return this;
    }

}
