package com.example.manmu;

import com.example.manmu.entity.Room;
import com.example.manmu.entity.Song;
import com.example.manmu.entity.User;
import com.example.manmu.repository.RoomRepository;
import com.example.manmu.repository.TestRepository;
import com.example.manmu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RoomService {
//    @Autowired
//    private final RoomRepository roomRepository;
    @Autowired
    private final TestRepository testRepository;


//    public Room createRoom(String userId, List<Song> songs) {
//        Room room = Room.builder()
//                .roomId(UUID.randomUUID().toString())
//                .state(RoomState.WAITING)
//                .songs(songs)
//                .userCount(1)
//                .build();
//        return roomRepository.save(room);
//    }

    public Room createRoomTest(String userId, List<Song> songs) {
        Room room = Room.builder()
                .roomId(UUID.randomUUID().toString())
                .state(RoomState.WAITING)
                .songs(songs)
                .userCount(1)
                .build();
        return testRepository.save(room);
    }

//    public Room matchRoom(List<Song> songs, String userId) {
//        // TODO: implement enterRoom method when user enters room.
//        List<Room> rooms = roomRepository.findAll();
//        for (Room room : rooms) {
//            List<Song> roomSongsCopy = new ArrayList<>(room.getSongs());
//            roomSongsCopy.retainAll(songs);
//            if (roomSongsCopy.size() == songs.size() && room.getUserCount()<2) {
//                room.setUserCount(room.getUserCount() + 1);
//                roomRepository.save(room);
//                return room;
//            }
//        }
//        return createRoom(userId, songs);
//    }

    public Room matchRoomTest(List<Song> songs, String userId) {
        // TODO: implement enterRoom method when user enters room.
        List<Room> rooms = testRepository.findAll();
        for (Room room : rooms) {
            List<Song> roomSongsCopy = new ArrayList<>(room.getSongs());
            roomSongsCopy.retainAll(songs);
            if (roomSongsCopy.size() == songs.size()) {
                if(room.getUserCount() < 2) {
                    room.setUserCount(room.getUserCount() + 1);
                    testRepository.save(room);
                    return room;
                }
            }
        }
        return createRoomTest(userId, songs);
    }

//    public Room enterRoom(String roomId, String userId) {
//        Optional<Room> room = roomRepository.findById(roomId);
//        if (room.isPresent()) {
//            Room foundRoom = room.get();
//            foundRoom.setUserCount(foundRoom.getUserCount() + 1);
//            return foundRoom;
//        }
//        return null;
//    }

    public Room enterRoomTest(String roomId, String userId, String direction) {
        if ((userId == null) && (roomId.equals("default"))) {
            return testRepository.getFirst();
        }
        if (direction.equals("current")) {
            Room foundRoom = testRepository.findById(roomId);
            if (foundRoom != null) {
                foundRoom.setUserCount(foundRoom.getUserCount() + 1);
                return foundRoom;
            }
        } else if (direction.equals("next")) {
            Room currentRoom = testRepository.findById(roomId);
            Room nextRoom = testRepository.getNext(roomId);
            currentRoom.setUserCount(currentRoom.getUserCount() - 1);
            nextRoom.setUserCount(nextRoom.getUserCount() + 1);
            return nextRoom;
        } else {
            Room currentRoom = testRepository.findById(roomId);
            Room prevRoom = testRepository.getPrev(roomId);
            currentRoom.setUserCount(currentRoom.getUserCount() - 1);
            prevRoom.setUserCount(prevRoom.getUserCount() + 1);
            return prevRoom;
        }
        return null;
    }

//    public List<Room> findAllRooms() {
//        return roomRepository.findAll();
//    }

    public List<Room> findAllRoomsTest() {
        return testRepository.findAll();
    }


//    public List<Room> findRoomsByStatePlaying() {
//        return roomRepository.findByState(RoomState.PLAYING);
//    }

    public List<Room> findRoomsByStatePlayingTest() {
        return testRepository.findByState(RoomState.PLAYING);
    }

//    public Room startBroadcasting(String roomId) {
//        Room room = roomRepository.findById(roomId).get();
//        room.setState(RoomState.PLAYING);
//        roomRepository.save(room);
//        return room;
//    }

    public Room startBroadcastingTest(String roomId) {
        Room room = testRepository.findById(roomId);
        room.setState(RoomState.PLAYING);
        testRepository.save(room);
        return room;
    }
//    public List<Room> exitBroadcast(String roomId) {
//        roomRepository.deleteById(roomId);
//        return roomRepository.findAll();
//    }

    public List<Room> exitBroadcastTest(String roomId) {
        testRepository.delete(roomId);
        return testRepository.findAll();
    }

}
