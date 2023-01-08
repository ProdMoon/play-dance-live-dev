package com.example.manmu.repository;

import com.example.manmu.entity.Room;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BroadcastingRepository {
    public Room save(Room room);
    public Room delete(String roomId);
    public Room findRoom(String roomId);
    public List<Room> findAll();
}
