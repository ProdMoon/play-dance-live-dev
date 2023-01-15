package com.example.manmu.repository;

import com.example.manmu.RoomState;
import com.example.manmu.entity.Room;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.synchronizedList;

@Repository
public class TestRepository {

    List<Room> rooms = synchronizedList(new LinkedList<>());

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

    public Room getNext(String roomId) {
        if (rooms.isEmpty()) {
            return null;
        }
        Room currentRoom = rooms.stream()
                .filter(room -> room.getRoomId().equals(roomId))
                .findFirst()
                .orElse(null);
        if (currentRoom == null) {
            return null;
        }
        int index = rooms.indexOf(currentRoom);
        int nextIndex = findNextIndex(index);
        return rooms.get(nextIndex);
    }

    private int findNextIndex(int currentIndex) {
        int nextIndex = currentIndex + 1;
        if (nextIndex >= rooms.size()) {
            nextIndex = 0;
        }
        while (rooms.get(nextIndex) == null) {
            nextIndex++;
            if (nextIndex >= rooms.size()) {
                nextIndex = 0;
            }
        }
        return nextIndex;
    }

    public Room getPrev(String roomId) {
        if (rooms.isEmpty()) {
            return null;
        }
        Room currentRoom = rooms.stream()
                .filter(room -> room.getRoomId().equals(roomId))
                .findFirst()
                .orElse(null);
        if (currentRoom == null) {
            return null;
        }
        int index = rooms.indexOf(currentRoom);
        if (index == 0) {
            return currentRoom;
        } else {
            return rooms.get((index - 1) % rooms.size());
        }
    }

    public Room getFirst() {
        if (rooms.size() >= 1) {
            return rooms.get(0);
        }
        return null;
    }
}
