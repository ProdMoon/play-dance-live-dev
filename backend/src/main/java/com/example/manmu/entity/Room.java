package com.example.manmu.entity;

import com.example.manmu.RoomState;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import static java.util.Collections.synchronizedList;

@Getter
@Setter
@NoArgsConstructor
public class Room {
    @Id
    private String roomId;
    @Column
    private Integer userCount;
    @Column
    private RoomState state;
    @Column
    private List<Song> songs = synchronizedList(new ArrayList<>());
    @Column
    private boolean isEmpty;

    @Builder
    public Room(String roomId, Integer userCount ,RoomState state, List<Song> songs) {
        this.roomId = roomId;
        this.userCount = userCount;
        this.state = state;
        this.songs = songs;
    }
}
