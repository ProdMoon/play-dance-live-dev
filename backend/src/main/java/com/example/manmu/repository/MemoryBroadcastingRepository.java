package com.example.manmu.repository;

import com.example.manmu.entity.Room;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import static java.util.Collections.*;


@Repository
public class MemoryBroadcastingRepository implements BroadcastingRepository {

    private final List<Room> store = synchronizedList(new ArrayList<>());
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
