package com.example.manmu.controller;

import com.example.manmu.RoomService;
import com.example.manmu.entity.Room;
import com.example.manmu.entity.Song;
import com.example.manmu.entity.User;
import io.openvidu.java.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.synchronizedList;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class RoomController {
    private RoomService roomService;
    private String OPENVIDU_URL;
    private String OPENVIDU_SECRET;
    private OpenVidu openvidu;
    private List<Room> rooms = synchronizedList(new LinkedList<>());

    public RoomController(RoomService roomService,  @Value("${OPENVIDU_URL}")String openviduUrl, @Value("${OPENVIDU_SECRET}")String openviduSecret, List<Room>rooms) {
        this.roomService = roomService;
        this.OPENVIDU_URL = openviduUrl;
        this.OPENVIDU_SECRET = openviduSecret;
        this.openvidu = new OpenVidu(openviduUrl, openviduUrl);
        this.rooms = rooms;
    }

    public ResponseEntity<String> makeRoom(@RequestBody(required = false) Map<String, Object> params, User user, List<Song> songs)
            throws OpenViduJavaClientException, OpenViduHttpException {
        String roomId = UUID.randomUUID().toString();
        SessionProperties properties = SessionProperties.fromJson(params).build();
        Session session = openvidu.createSession(properties);
        Room room = Room.builder()
                .roomId(roomId)
                .sessionId(session.getSessionId())
                .build();
        roomService.createRoom(room.getRoomId(), user, songs);
        return new ResponseEntity<>(session.getSessionId(), HttpStatus.OK);
    }

    @PostMapping("/api/sessions/{sessionId}/connections")
    public ResponseEntity<String> createConnection(@PathVariable("sessionId") String sessionId,
                                                   @RequestBody(required = false) Map<String, Object> params)
            throws OpenViduJavaClientException, OpenViduHttpException {
        Session session = openvidu.getActiveSession(sessionId);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
        Connection connection = session.createConnection(properties);
        return new ResponseEntity<>(connection.getToken(), HttpStatus.OK);
    }

    @PostMapping("/api/sessions/{sessionId}/enter")
    public ResponseEntity<String> enterRoom(@PathVariable("sessionId") String sessionId,
                                                   @RequestBody(required = false) Map<String, Object> params, Long id)
            throws OpenViduJavaClientException, OpenViduHttpException {
        Session session = openvidu.getActiveSession(sessionId);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
        Connection connection = session.createConnection(properties);
        return new ResponseEntity<>(connection.getToken(), HttpStatus.OK);
    }

//    @PostMapping("/api/sessions/{sessionId}/ready")
//    public ResponseEntity<String> readyRoom(@PathVariable("sessionId") String sessionId,
//                                            @RequestBody(required = false) Map<String, Object> params, Long id)
//            throws OpenViduJavaClientException, OpenViduHttpException {
//        return new ResponseEntity<>(connection.getToken(), HttpStatus.OK);
//    }

    @PostMapping("/api/sessions/{sessionId}/quit")
    public ResponseEntity<String> quitRoom(@PathVariable("sessionId") String sessionId,
                                            @RequestBody(required = false) Map<String, Object> params, Long userId) {
        Room room = roomService.findbyUserId(userId);
        if (room == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        roomService.quitRoom(room, rooms);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
