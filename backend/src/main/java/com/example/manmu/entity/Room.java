package com.example.manmu.entity;

import com.example.manmu.RoomState;
import io.openvidu.java.client.Session;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.synchronizedList;

@Getter
@NoArgsConstructor
@Table(name= "room")
@Entity
public class Room {
    @Id
    private String roomId;
    @Column
    private Integer userCount;
    @Column
    private RoomState state;
    @Column(name = "songs")
    @OneToMany(mappedBy = "room")
    private List<Song> songs;


    @Builder
    public Room(String roomId, Integer userCount ,RoomState state, List<Song> songs) {
        this.roomId = roomId;
        this.userCount = userCount;
        this.state = state;
        this.songs = songs;
    }

}
