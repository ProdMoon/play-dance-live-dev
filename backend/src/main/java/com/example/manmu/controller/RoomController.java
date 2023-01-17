package com.example.manmu.controller;

import com.example.manmu.service.GameRoomService;
import com.example.manmu.entity.RoomDto;
import com.example.manmu.entity.Song;
import com.example.manmu.service.RoomHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
public class RoomController {

    private final GameRoomService gameRoomService;
    private final RoomHistoryService roomHistoryService;

    @PostMapping("/api/room/create")
    public ResponseEntity<RoomDto> createRoom(@RequestBody(required = false) Map<String, Object> params) {
        String userId = (String) params.get("userId");
        List<String> songs = (List<String>) params.get("songs");
        RoomDto roomDto = gameRoomService.createRoom(userId, songs);
        if (roomDto != null) {
            return new ResponseEntity<>(roomDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/api/room/match")
    public ResponseEntity<RoomDto> TestMatchRoom(@RequestBody Map<String, Object> params){
        String userId = (String) params.get("userId");
        List<String> songs = (List<String>) params.get("songs");
        RoomDto roomDto = gameRoomService.matchRoom(songs, userId);
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
        RoomDto roomDto = gameRoomService.enterRoom(roomId, userId, direction);
        return new ResponseEntity<>(roomDto, HttpStatus.OK);
    }

    @PostMapping("/api/room/leave")
    public ResponseEntity<HttpStatus> leaveRoom(@RequestBody Map<String, Object> params) {
        String userId = (String) params.get("userId");
        String roomId = (String) params.get("roomId");
        gameRoomService.leaveRoom(roomId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/room/startPlaying")
    public ResponseEntity<RoomDto> startGame(@RequestBody Map<String, String> params){
        RoomDto roomDto =  gameRoomService.startPlaying(params.get("roomId"));
        return new ResponseEntity<>(roomDto, HttpStatus.OK);
    }


    @DeleteMapping("/api/room")
    public void endGame(@RequestBody Map<String, String> params){
        roomHistoryService.saveRoomHistory(params.get("roomId"));
        gameRoomService.endPlaying(params.get("roomId"));
    }

    @GetMapping("/api/room/getPlaying")
    public ResponseEntity<List<RoomDto>> getPlayingRoom(){
        List<RoomDto> roomDtos = gameRoomService.getPlayingRooms();
        return new ResponseEntity<>(roomDtos, HttpStatus.OK);
    }

    @GetMapping("/api/room/getWaiting")
    public ResponseEntity<List<RoomDto>> getWaitingRoom(){
        List<RoomDto> roomDtos = gameRoomService.getWaitingRooms();
        return new ResponseEntity<>(roomDtos, HttpStatus.OK);
    }
}
