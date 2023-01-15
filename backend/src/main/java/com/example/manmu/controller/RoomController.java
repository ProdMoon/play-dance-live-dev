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

//    @PostMapping("/api/room/create")
//    public Room createRoomControl(@RequestBody Map<String, Object> params) {
//        String userId = (String) params.get("userId");
//        List<Song> songs = (List<Song>) params.get("songs");
//        Room room = roomService.createRoom(userId, songs);
//        return room;
//    }

    @PostMapping("/api/room/create")
    public ResponseEntity<Room> TestCreateRoom(@RequestBody(required = false) Map<String, Object> params) {
        String userId = (String) params.get("userId");
        List<Song> songs = (List<Song>) params.get("songs");
        Room room = roomService.createRoomTest(userId, songs);
        if (room != null) {
            return new ResponseEntity<>(room, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

//    @PostMapping("/api/room/match")
//    public Room matchRoomControl(@RequestBody Map<String, Object> params){
//        String userId = (String) params.get("userId");
//        List<Song> songs = (List<Song>) params.get("songs");
//        return roomService.matchRoom(songs, userId);
//    }

    @PostMapping("/api/room/match")
    public ResponseEntity<Room> TestMatchRoom(@RequestBody Map<String, Object> params){
        String userId = (String) params.get("userId");
        List<Song> songs = (List<Song>) params.get("songs");
        Room room = roomService.matchRoomTest(songs, userId);
        if (room != null) {
            return new ResponseEntity<>(room, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

//    @PostMapping("/api/room/enter")
//    public Room enterRoomControl(@RequestBody Map<String, Object> params){
//        String userId = (String) params.get("userId");
//        String roomId = (String) params.get("roomId");
//        Room room = roomService.enterRoom(roomId, userId);
//        if(room != null) {
//            return room;
//        }
//        return null;
//    }

    @PostMapping("/api/room/enter")
    public ResponseEntity<Room> TestEnterRoom(@RequestBody Map<String, Object> params){
        String userId = (String) params.get("userId");
        String roomId = (String) params.get("roomId");
        String direction = (String) params.get("direction");
        Room room = roomService.enterRoomTest(roomId, userId, direction);
        if (room != null) {
            return new ResponseEntity<>(room, HttpStatus.OK);
        }
        room.setEmpty(true);
        return new ResponseEntity<>(room, HttpStatus.OK);
    }

//    @GetMapping("/api/room/findAllRooms")
//    public List<Room> findAllRoomsControl(){
//        return roomService.findAllRooms();
//    }

    @GetMapping("/api/room/findAllRooms")
    public ResponseEntity<List<Room>> TestFindAllRooms(){
        List<Room> allRooms =  roomService.findAllRoomsTest();
        return new ResponseEntity<>(allRooms, HttpStatus.OK);
    }

//    @GetMapping("/api/room/findAllPlaying")
//    public List<Room> findAllPlayingControl(){
//        return roomService.findRoomsByStatePlaying();
//    }


    @GetMapping("/api/room/findAllPlaying")
    public ResponseEntity<List<Room>> TestFindAllPlaying(){
        List<Room> allPlayingRooms = roomService.findRoomsByStatePlayingTest();
        return new ResponseEntity<>(allPlayingRooms, HttpStatus.OK);
    }

//    @PostMapping("/api/room/startBroadcast")
//    public Room StartBroadcastControl(@RequestBody Map<String, String> params){
//        return roomService.startBroadcasting(params.get("roomId"));
//    }

    @PostMapping("/api/room/startBroadcast")
    public ResponseEntity<Room> TestStartBroadcast(@RequestBody Map<String, String> params){
        Room room =  roomService.startBroadcastingTest(params.get("roomId"));
        return new ResponseEntity<>(room, HttpStatus.OK);
    }

//    @PostMapping("/api/room/exitBroadcast")
//    public List<Room> TestExitBroadcastControl(@RequestBody Map<String, String> params){
//        return roomService.exitBroadcast(params.get("roomId"));
//    }

    @PostMapping("/api/room/exitBroadcast")
    public ResponseEntity<List<Room>> TestExitBroadcast(@RequestBody Map<String, String> params){
        List<Room> rooms = roomService.exitBroadcastTest(params.get("roomId"));
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }
}
