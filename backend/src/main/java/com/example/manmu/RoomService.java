package com.example.manmu;

import com.example.manmu.entity.Room;
import com.example.manmu.entity.Song;
import com.example.manmu.entity.User;
import com.example.manmu.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class RoomService {
    private final RoomRepository roomRepository;
    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room createRoom(String roomId, User user, List<Song> songs) {
        Room room = Room.builder()
                .roomId(UUID.randomUUID().toString())
                .state(RoomState.WAITING)
                .songs(songs)
                .userCount(1)
                .build();
        room.getPublishers().add(user);
        return roomRepository.save(room);
    }

    public Optional<Room> enterRoom(String sessionId) {
        // TODO: implement enterRoom method when user enters room.
        Optional<Room> room = roomRepository.findById(sessionId);
        return room;
    }

//    public void goBroadcast(Room room) {
//        room.setState(RoomState.PLAYING);
//        broadcastingRepository.save(room);
//        waitingRepository.delete(room.getRoomId());
//    }

//    public void exitBroadcast(Room room, List<Room> rooms) {
//        room.setState(RoomState.END);
//        broadcastingRepository.delete(room.getRoomId());
//        rooms.remove(room);
//    }

//    public void quitRoom(Room room, List<Room> rooms) {
//        room.getPublishers().remove(room.getPublishers().size() - 1);
//        room.setPubCount(room.getPublishers().size());
//        if (room.getPubCount() == 0) {
//            waitingRepository.delete(room.getRoomId());
//            rooms.remove(room);
//        }
//    }

    public Room findBroadcastByRoomId(String roomId) {
        return broadcastingRepository.findRoom(roomId);
    }

    public Room findbyUserId(Long userId) {
        for (Room room : waitingRepository.findAll()) {
            for (User user : room.getPublishers()) {
                if (user.getId().equals(userId)) {
                    return room;
                }
            }
            for (User user : room.getSubscribers()) {
                if (user.getId().equals(userId)) {
                    return room;
                }
            }
        }
        return null;
    }

    public User getUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.orElse(null);
    }
}
