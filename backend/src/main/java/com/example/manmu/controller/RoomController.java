package com.example.manmu.controller;

import com.example.manmu.RoomService;
import com.example.manmu.entity.Room;
import com.example.manmu.entity.Song;
import com.example.manmu.entity.User;
import com.example.manmu.repository.UserRepository;
import io.openvidu.java.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static java.util.Collections.synchronizedList;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class RoomController {
    private final RoomService roomService;
    private String OPENVIDU_URL;
    private String OPENVIDU_SECRET;
    private OpenVidu openvidu;
    private final List<Room> rooms = synchronizedList(new LinkedList<>());


    @Autowired
    public RoomController(RoomService roomService,  @Value("${OPENVIDU_URL}")String openviduUrl, @Value("${OPENVIDU_SECRET}")String openviduSecret) {
        this.roomService = roomService;
        this.OPENVIDU_URL = openviduUrl;
        this.OPENVIDU_SECRET = openviduSecret;
        this.openvidu = new OpenVidu(openviduUrl, openviduSecret);
    }

    public ResponseEntity<String> makeRoom(@RequestBody(required = false) Map<String, Object> params)
            throws OpenViduJavaClientException, OpenViduHttpException {
        SessionProperties properties = SessionProperties.fromJson(params).build();
        Session session = openvidu.createSession(properties);
        Room room = Room.builder()
                .roomId(session.getSessionId())
                .build();
        List<Song> songs = params.get("songs") == null ? new ArrayList<>() : (List<Song>) params.get("songs");
        Long userId = params.get("userid") == null ? null : (Long) params.get("userid");
        User user = roomService.getUserById(userId);
        roomService.createRoom(room.getRoomId(), user, songs);
        return new ResponseEntity<>(session.getSessionId(), HttpStatus.OK);
    }

    @PostMapping("/api/sessions/{sessionId}/connection")
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

    @PostMapping("/api/sessions/{beforeSessionId}/enter")
    public ResponseEntity<String> enterRoom(@PathVariable("sessionId") String beforeSessionId,
                                                   @RequestBody(required = false) Map<String, Object> params, Long id)
            throws OpenViduJavaClientException, OpenViduHttpException {
        Room room = roomService.findBroadcastByRoomId(beforeSessionId);
        Session session = openvidu.getActiveSession(sessionId);
        if (session == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
        Connection connection = session.createConnection(properties);
        return new ResponseEntity<>(connection.getToken(), HttpStatus.OK);
    }

    @PostMapping("/api/sessions/{sessionId}/ready")
    public ResponseEntity<String> readyRoom(@PathVariable("sessionId") String sessionId,
                                            @RequestBody(required = false) Map<String, Object> params, Long id)
            throws OpenViduJavaClientException, OpenViduHttpException {
        Room room = roomService.findBroadcastByRoomId(sessionId);
        roomService.goBroadcast(room);
        return new ResponseEntity<>(connection.getToken(), HttpStatus.OK);
    }

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
