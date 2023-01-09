package com.example.manmu;

import com.example.manmu.entity.Room;
import com.example.manmu.entity.Song;
import com.example.manmu.entity.User;
import com.example.manmu.repository.BroadcastingRepository;
import com.example.manmu.repository.UserRepository;
import com.example.manmu.repository.WaitingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class RoomService {
    private final WaitingRepository waitingRepository;
    private final BroadcastingRepository broadcastingRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoomService(WaitingRepository waitingRepository, BroadcastingRepository broadcastingRepository, UserRepository userRepository) {
        this.waitingRepository = waitingRepository;
        this.broadcastingRepository = broadcastingRepository;
        this.userRepository = userRepository;
    }

    public Room createRoom(String roomId, User user, List<Song> songs) {
        Room room = Room.builder()
                .roomId(UUID.randomUUID().toString())
                .state(RoomState.WAITING)
                .songs(songs)
                .build();
        room.getPublishers().add(user);
        room.setPubCount(room.getPublishers().size());
        return waitingRepository.save(room);
    }

    public Room enterRoom(List<Room> rooms, User user, List<Song> songs) {
        // TODO: implement enterRoom method when user enters room.
        return null;
    }

    public void goBroadcast(Room room) {
        room.setState(RoomState.PLAYING);
        broadcastingRepository.save(room);
        waitingRepository.delete(room.getRoomId());
    }

    public void exitBroadcast(Room room, List<Room> rooms) {
        room.setState(RoomState.END);
        broadcastingRepository.delete(room.getRoomId());
        rooms.remove(room);
    }

    public void quitRoom(Room room, List<Room> rooms) {
        room.getPublishers().remove(room.getPublishers().size() - 1);
        room.setPubCount(room.getPublishers().size());
        if (room.getPubCount() == 0) {
            waitingRepository.delete(room.getRoomId());
            rooms.remove(room);
        }
    }

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
