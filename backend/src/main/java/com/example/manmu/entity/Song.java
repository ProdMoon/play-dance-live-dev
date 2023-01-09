package com.example.manmu.entity;

import javax.persistence.*;

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
