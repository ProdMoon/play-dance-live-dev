package com.example.manmu.controller;

import com.example.manmu.RoomService;
import com.example.manmu.entity.Room;
import io.openvidu.java.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final Integer MAX_DANCER = 2;
    private RoomService roomService;
    private String OPENVIDU_URL;
    private String OPENVIDU_SECRET;
    private OpenVidu openvidu;


    @Autowired
    public void RoomController(RoomService roomService,  @Value("${OPENVIDU_URL}")String openviduUrl, @Value("${OPENVIDU_SECRET}")String openviduSecret) {
        this.roomService = roomService;
        this.OPENVIDU_URL = openviduUrl;
        this.OPENVIDU_SECRET = openviduSecret;
        this.openvidu = new OpenVidu(openviduUrl, openviduUrl);
    }

    @PostMapping("/api/sessions}")
    public ResponseEntity<String> makeRoom(@RequestBody(required = false) Map<String, Object> params, Long id)
            throws OpenViduJavaClientException, OpenViduHttpException {
        String roomId = UUID.randomUUID().toString();
        roomService.createRoom(roomId, id);
        SessionProperties properties = SessionProperties.fromJson(params).build();
        Session session = openvidu.createSession(properties);
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

    @PostMapping("/api/sessions/{sessionId}/connections")
    public ResponseEntity<String> enterRoom(@PathVariable("sessionId") String sessionId,
                                                   @RequestBody(required = false) Map<String, Object> params, Long id)
            throws OpenViduJavaClientException, OpenViduHttpException {
        Session session = openvidu.getActiveSession(sessionId);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
        Connection connection = session.createConnection(properties);
        roomService.enterRoom(id);
        return new ResponseEntity<>(connection.getToken(), HttpStatus.OK);
    }

    @PostMapping("/api/sessions/{sessionId}/disconnection")
    public ResponseEntity<String> quitRoom(@PathVariable("sessionId") String sessionId,
                                            @RequestBody(required = false) Map<String, Object> params, Long id)
            throws OpenViduJavaClientException, OpenViduHttpException {
        Room room = roomService.findbyId(id);
        if (room == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        roomService.quitRoom(id , room.getRoomId());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
