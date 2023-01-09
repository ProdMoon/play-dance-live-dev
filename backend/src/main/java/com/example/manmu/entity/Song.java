package com.example.manmu.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Table(name= "song")
@Entity
public class Song {
    @Id
    private Integer songId;
    @Column
    private String songName;
    @Column
    private String singer;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
}
