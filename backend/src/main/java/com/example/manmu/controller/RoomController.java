package com.example.manmu.controller;

import com.example.manmu.GameSignal;
import com.example.manmu.entity.VoteData;
import com.example.manmu.service.GameRoomService;
import com.example.manmu.entity.RoomDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
public class RoomController {

    private final GameRoomService gameRoomService;
    private final SimpMessagingTemplate template;

    @PostMapping("/api/room/create")
    public ResponseEntity<RoomDto> createRoom(@RequestBody(required = false) Map<String, Object> params) {
        String userMail = (String) params.get("userMail");
        String roomSongs = (String) params.get("roomSong");
        RoomDto roomDto = gameRoomService.createRoom();
        if (roomDto != null) {
            return new ResponseEntity<>(roomDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @MessageMapping("/join")
    public void joinGame(@Payload GameSignal gameSignal) {
        String userMail = gameSignal.getSender();
        RoomDto joinRoomDto = gameRoomService.joinGame(userMail);
        if (joinRoomDto != null){
            gameSignal.setType("REFRESH_WAITER_LIST");
            gameSignal.setWaiters(joinRoomDto.getWaiters());
            template.convertAndSend("/topic/public", gameSignal);
        }
    }

    @MessageMapping("/start")
    public void startGame(@Payload GameSignal gameSignal) {
        RoomDto startRoomDto = gameRoomService.startGame();
        if (startRoomDto != null){
            gameSignal.setType("GAME_START");
            gameSignal.setWaiters(startRoomDto.getWaiters());
            gameSignal.setChampion(startRoomDto.getCurrentChampion());
            gameSignal.setChallenger(startRoomDto.getCurrentChallenger());
            template.convertAndSend("/topic/public", gameSignal);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            gameSignal.setType("REFRESH_WAITER_LIST");
            template.convertAndSend("/topic/public", gameSignal);
        }
    }


//    @PostMapping("/api/room/match")
//    public ResponseEntity<RoomDto> TestMatchRoom(@RequestBody Map<String, Object> params) {
//        String userMail = (String) params.get("userId");
//        List<String> songs = (List<String>) params.get("songs");
//        RoomDto roomDto = gameRoomService.matchRoom(songs, userMail);
//        if (roomDto != null) {
//            return new ResponseEntity<>(roomDto, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }

    @PostMapping("/api/room")
    public ResponseEntity<RoomDto> EnterRoom(@RequestBody Map<String, Object> params) {
        String userMail = (String) params.get("userId");
        RoomDto roomDto = gameRoomService.enterRoom(userMail);
        return new ResponseEntity<>(roomDto, HttpStatus.OK);
    }

//    @PostMapping("/api/room/leave")
//    public ResponseEntity<HttpStatus> leaveRoom(@RequestBody Map<String, Object> params) {
//        String userMail = (String) params.get("userId");
//        String roomId = (String) params.get("roomId");
//        gameRoomService.leaveRoom(roomId, userId);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }

//    @PostMapping("/api/room/startPlaying")
//    public ResponseEntity<RoomDto> startGame(@RequestBody Map<String, String> params) {
//        RoomDto roomDto = gameRoomService.startPlaying(params.get("roomId"));
//        return new ResponseEntity<>(roomDto, HttpStatus.OK);
//    }


    @MessageMapping("/game-end")
    public void endGame(@Payload GameSignal gameSignal) {
        String userMail = gameSignal.getSender();
        RoomDto gameRoomResult = gameRoomService.endGame(userMail);
        if (gameRoomResult == null){
            gameSignal.setType("NEXT_ROUND");
            template.convertAndSend("/topic/public", gameSignal);
        }
        if (gameRoomResult != null) {
            gameSignal.setType("GAME_END");
            gameSignal.setChampion(gameRoomResult.getCurrentChampion());
            gameSignal.setRankingList(gameRoomResult.getRankingList());
            template.convertAndSend("/topic/public", gameSignal);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            gameSignal.setType("GAME_CHALLENGE");
            gameSignal.setChallenger(gameRoomResult.getCurrentChallenger());
            template.convertAndSend("/topic/public", gameSignal);
        }
    }

    @MessageMapping("/vote")
    public void vote(@Payload VoteData VoteData) {
        String type = VoteData.getType();
        String sender = VoteData.getSender();
        String winner = VoteData.getWinner();
        Integer pollLeft = VoteData.getPollLeft();
        Integer pollRight = VoteData.getPollRight();
        gameRoomService.vote(VoteData);
        template.convertAndSend("/topic/public", VoteData);
    }
}
