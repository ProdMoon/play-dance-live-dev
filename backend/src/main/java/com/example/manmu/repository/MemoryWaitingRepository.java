package com.example.manmu.repository;

import com.example.manmu.entity.Room;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Repository
public class MemoryWaitingRepository implements WaitingRepository {
    private List<Room> store;

    public void init() {
        store = Collections.synchronizedList(new ArrayList<>());
    }

    @Override
    public Room save(Room room) {
        store.add(room);
        return room;
    }

    @Override
    public Room delete(String roomId) {
        Room room = findRoom(roomId);
        store.remove(room);
        return room;
    }

    @Override
    public Room findRoom(String roomId) {
        return store.stream()
                .filter(room -> room.getRoomId().equals(roomId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Room> findAll() {
        return store;
    }
}
