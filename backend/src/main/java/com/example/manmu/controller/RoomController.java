package com.example.manmu.controller;

import com.example.manmu.service.RoomService;
import com.example.manmu.entity.RoomDto;
import com.example.manmu.entity.Song;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
public class RoomController {
    @Autowired
    private final RoomService roomService;

    @PostMapping("/api/room/create")
    public ResponseEntity<RoomDto> createRoom(@RequestBody(required = false) Map<String, Object> params) {
        String userId = (String) params.get("userId");
        List<Song> songs = (List<Song>) params.get("songs");
        RoomDto roomDto = roomService.createRoom(userId, songs);
        if (roomDto != null) {
            return new ResponseEntity<>(roomDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/api/room/match")
    public ResponseEntity<RoomDto> TestMatchRoom(@RequestBody Map<String, Object> params){
        String userId = (String) params.get("userId");
        List<Song> songs = (List<Song>) params.get("songs");
        RoomDto roomDto = roomService.matchRoom(songs, userId);
        if (roomDto != null) {
            return new ResponseEntity<>(roomDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/api/room/enter")
    public ResponseEntity<RoomDto> EnterRoom(@RequestBody Map<String, Object> params){
        String userId = (String) params.get("userId");
        String roomId = (String) params.get("roomId");
        String direction = (String) params.get("direction");
        RoomDto roomDto = roomService.enterRoom(roomId, userId, direction);
        return new ResponseEntity<>(roomDto, HttpStatus.OK);
    }

    @PostMapping("/api/room/leave")
    public ResponseEntity<HttpStatus> leaveRoom(@RequestBody Map<String, Object> params) {
        String userId = (String) params.get("userId");
        String roomId = (String) params.get("roomId");
        roomService.leaveRoom(roomId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/room/startPlaying")
    public ResponseEntity<RoomDto> startBroadcast(@RequestBody Map<String, String> params){
        RoomDto roomDto =  roomService.startPlaying(params.get("roomId"));
        return new ResponseEntity<>(roomDto, HttpStatus.OK);
    }


    @PostMapping("/api/room/exitPlaying")
    public void TestExitBroadcast(@RequestBody Map<String, String> params){
        roomService.exitPlaying(params.get("roomId"));
    }

    @GetMapping("/api/room/getPlaying")
    public ResponseEntity<List<RoomDto>> getPlayingRoom(){
        List<RoomDto> roomDtos = roomService.getPlayingRooms();
        return new ResponseEntity<>(roomDtos, HttpStatus.OK);
    }

    @GetMapping("/api/room/getWaiting")
    public ResponseEntity<List<RoomDto>> getWaitingRoom(){
        List<RoomDto> roomDtos = roomService.getWaitingRooms();
        return new ResponseEntity<>(roomDtos, HttpStatus.OK);
    }
}
