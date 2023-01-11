package com.example.manmu.repository;

import com.example.manmu.RoomState;
import com.example.manmu.entity.Room;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TestRepository {
    List<Room> rooms = new ArrayList<>();

    public Room save(Room room) {
        rooms.add(room);
        return room;
    }

    public List<Room> findAll() {
        return rooms;
    }

    public List<Room> findByState(RoomState state) {
        List<Room> result = new ArrayList<>();
        for (Room room : rooms) {
            if (room.getState() == state) {
                result.add(room);
            }
        }
        return result;
    }

    public Room findById(String roomId){
        for (Room room : rooms) {
            if (room.getRoomId().equals(roomId)) {
                return room;
            }
        }
        return null;
    }

    public List<Room> delete(String roomId) {
        for (Room room : rooms) {
            if (room.getRoomId().equals(roomId)) {
                rooms.remove(room);
                return rooms;
            }
        }
        rooms.remove(roomId);
        return rooms;
    }
}
