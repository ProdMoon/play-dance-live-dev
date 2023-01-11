package com.example.manmu.controller;

import com.example.manmu.RoomService;
import com.example.manmu.RoomState;
import com.example.manmu.entity.Room;
import com.example.manmu.entity.Song;
import com.example.manmu.entity.User;
import com.example.manmu.repository.RoomRepository;
import com.example.manmu.repository.UserRepository;
import io.openvidu.java.client.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static java.util.Collections.synchronizedList;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
public class RoomController {
    @Autowired
    private final RoomService roomService;

    @PostMapping("/api/room/create")
    public Room createRoomControl(@RequestBody Map<String, Object> params) {
        String userId = (String) params.get("userId");
        List<Song> songs = (List<Song>) params.get("songs");
        Room room = roomService.createRoom(userId, songs);
        return room;
    }

//    @PostMapping("/api/create")
//    public Room TestCreateRoom(@RequestBody(required = false) Map<String, Object> params) {
//        String userId = (String) params.get("userId");
//        List<Song> songs = (List<Song>) params.get("songs");
//        Room room = roomService.createRoomTest(userId, songs);
//        return room;
//    }

    @PostMapping("/api/room/match")
    public Room matchRoomControl(@RequestBody Map<String, Object> params){
        String userId = (String) params.get("userId");
        List<Song> songs = (List<Song>) params.get("songs");
        return roomService.matchRoom(songs, userId);
    }

//    @PostMapping("/api/match")
//    public Room TestMatchRoom(@RequestBody Map<String, Object> params){
//        String userId = (String) params.get("userId");
//        List<Song> songs = (List<Song>) params.get("songs");
//        return roomService.matchRoomTest(songs, userId);
//    }

    @PostMapping("/api/room/enter")
    public Room enterRoomControl(@RequestBody Map<String, Object> params){
        String userId = (String) params.get("userId");
        String roomId = (String) params.get("roomId");
        Room room = roomService.enterRoom(roomId, userId);
        if(room != null) {
            return room;
        }
        return null;
    }

//    @PostMapping("/api/enter")
//    public Room TestEnterRoom(@RequestBody Map<String, Object> params){
//        String userId = (String) params.get("userId");
//        String roomId = (String) params.get("roomId");
//        Room room = roomService.enterRoomTest(roomId, userId);
//        if(room != null) {
//            return room;
//        }
//        return null;
//    }

    @GetMapping("/api/findAllRooms")
    public List<Room> findAllRoomsControl(){
        return roomService.findAllRooms();
    }

//    @GetMapping("/api/findAllRooms")
//    public List<Room> TestfindAllRooms(){
//        return roomService.findAllRoomsTest();
//    }

    @GetMapping("/api/findAllPlaying")
    public List<Room> findAllPlayingControl(){
        return roomService.findRoomsByStatePlaying();
    }


//    @GetMapping("/api/findAllPlaying")
//    public List<Room> TestfindAllPlaying(){
//        return roomService.findRoomsByStatePlayingTest();
//    }

    @PostMapping("/api/startBroadcast")
    public Room StartBroadcastControl(@RequestBody Map<String, String> params){
        return roomService.startBroadcasting(params.get("roomId"));
    }
//    @PostMapping("/api/startBroadcast")
//    public Room TestStartBroadcast(@RequestBody Map<String, String> params){
//        return roomService.startBroadcastingTest(params.get("roomId"));
//    }

    @PostMapping("/api/exitBroadcast")
    public List<Room> TestExitBroadcastControl(@RequestBody Map<String, String> params){
        return roomService.exitBroadcast(params.get("roomId"));
    }

//    @PostMapping("/api/exitBroadcast")
//    public List<Room> TestExitBroadcast(@RequestBody Map<String, String> params){
//        return roomService.exitBroadcastTest(params.get("roomId"));
//    }
}
