package com.example.manmu;

import com.example.manmu.entity.Room;
import com.example.manmu.repository.BroadcastingRepository;
import com.example.manmu.repository.WaitingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

import static java.util.Collections.synchronizedList;


@Service
public class RoomService {
    private final WaitingRepository waitingRepository;
    private final BroadcastingRepository broadcastingRepository;

    @Autowired
    public RoomService(WaitingRepository waitingRepository, BroadcastingRepository broadcastingRepository) {
        this.waitingRepository = waitingRepository;
        this.broadcastingRepository = broadcastingRepository;
    }

    public Room createRoom(String roomId, Long userid) {
        Room room = Room.builder()
                .roomId(UUID.randomUUID().toString())
                .state(RoomState.WAITING)
                .players(synchronizedList(new ArrayList<>()))
                .songs(synchronizedList(new ArrayList<>()))
                .build();
        room.getPlayers().add(userid);
        return waitingRepository.save(room);
    }

    public Room enterRoom(Long userid) {
        for (Room room : waitingRepository.findAll()) {
            if (room.getPlayers().size() == 1) {
                room.getPlayers().add(userid);
                return room;
            }
        }
        String newRoomId = UUID.randomUUID().toString();
        return createRoom(newRoomId, userid);
    }

    public void quitRoom(Long id, String roomId) {
        Room room = waitingRepository.findRoom(roomId);
        room.getPlayers().remove(id);
        if (room.getPlayers().size() == 0) {
            waitingRepository.delete(roomId);
        }
    }

    public void goBroadcast(Room room) {
        room.setState(RoomState.PLAYING);
        broadcastingRepository.save(room);
        waitingRepository.delete(room.getRoomId());
    }

    public void exitBroadcast(Room room) {
        room.setState(RoomState.END);
        broadcastingRepository.delete(room.getRoomId());
    }

    public Room findbyId(Long id) {
        for (Room room : waitingRepository.findAll()) {
            if (room.getPlayers().contains(id)) {
                return room;
            }
        }
        for (Room room : broadcastingRepository.findAll()) {
            if (room.getPlayers().contains(id)) {
                return room;
            }
        }
        return null;
    }
}
