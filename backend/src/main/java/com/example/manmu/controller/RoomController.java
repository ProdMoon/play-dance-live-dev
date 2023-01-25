package com.example.manmu.controller;

import com.example.manmu.GameSignal;
import com.example.manmu.PollSignal;
import com.example.manmu.entity.UserDto;
import com.example.manmu.entity.VoteData;
import com.example.manmu.service.GameRoomService;
import com.example.manmu.entity.RoomDto;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.jni.Poll;
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
        String userSong = gameSignal.getSong();
        String userConnectionId = gameSignal.getConnectionId();
        RoomDto gameRoomDto = gameRoomService.joinGame(userMail, userSong, userConnectionId);
        gameSignal.setType("REFRESH_WAITER_LIST");
        gameSignal.setWaiters(gameRoomDto.getWaiters());
        gameSignal.setChampion(gameRoomDto.getCurrentChampion());
        gameSignal.setChallenger(gameRoomDto.getCurrentChallenger());
        gameSignal.setRankingList(gameRoomDto.getRankingList());
        template.convertAndSend("/topic/public", gameSignal);
        try{
        Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            PollSignal pollSignal = gameRoomService.getCurrentPoll();
            template.convertAndSend("/topic/public", pollSignal);
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
            } finally {
                gameSignal.setType("REFRESH_WAITER_LIST");
                template.convertAndSend("/topic/public", gameSignal);
            }

        }
    }

    @MessageMapping("/song/start")
    public void sendChampionSong(@Payload GameSignal gameSignal) {
        UserDto championUserDto = gameRoomService.findRoom().getCurrentChampion();
        gameSignal.setSong(championUserDto.getSong());
        gameSignal.setConnectionId(championUserDto.getConnectionId());
        template.convertAndSend("/topic/public", gameSignal);
    }

    @MessageMapping("/song/end")
    public void endSong(@Payload GameSignal gameSignal) {
        String userMail = gameSignal.getSender();
        RoomDto gameRoomDto = gameRoomService.endGame(userMail);
        if (gameRoomDto == null) {
            RoomDto roomDto = gameRoomService.findRoom();
            UserDto challengerDto = roomDto.getCurrentChallenger();
            gameSignal.setType("SONG_START");
            gameSignal.setConnectionId(challengerDto.getConnectionId());
            gameSignal.setSong(challengerDto.getSong());
            template.convertAndSend("/topic/public", gameSignal);
        }
        else  {
            gameSignal.setType("GAME_END");
            gameSignal.setChampion(gameRoomDto.getCurrentChampion());
            gameSignal.setRankingList(gameRoomDto.getRankingList());
            template.convertAndSend("/topic/public", gameSignal);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            gameSignal.setType("GAME_CHALLENGE");
            gameSignal.setWaiters(gameRoomDto.getWaiters());
            gameSignal.setChallenger(gameRoomDto.getCurrentChallenger());
            template.convertAndSend("/topic/public", gameSignal);
        }
    }


    @PostMapping("/api/room")
    public ResponseEntity<RoomDto> EnterRoom(@RequestBody Map<String, Object> params) {
        String userMail = (String) params.get("userId");
        RoomDto roomDto = gameRoomService.enterRoom(userMail);
        return new ResponseEntity<>(roomDto, HttpStatus.OK);
    }

    @PostMapping("/api/room/leave")
    public ResponseEntity<HttpStatus> leaveRoom(@RequestBody Map<String, Object> params) {
        String userMail = (String) params.get("userId");
        gameRoomService.leaveRoom(userMail);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @MessageMapping("/vote")
    public void vote(@Payload VoteData VoteData) {
        String type = VoteData.getType();
        String sender = VoteData.getSender();
        Integer pollLeft = VoteData.getPollLeft();
        Integer pollRight = VoteData.getPollRight();
        gameRoomService.vote(VoteData);
        template.convertAndSend("/topic/public", VoteData);
    }
}
