package com.example.manmu.repository;

import com.example.manmu.entity.Room;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface WaitingRepository {
    Room save(Room room);
    Room delete(String roomId);
    Room findRoom(String roomId);
    List<Room> findAll();
}

