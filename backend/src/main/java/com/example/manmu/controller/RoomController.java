package com.example.manmu.controller;

import com.example.manmu.RoomService;
import com.example.manmu.RoomState;
import com.example.manmu.entity.Room;
import com.example.manmu.entity.Song;
import com.example.manmu.entity.User;
import com.example.manmu.repository.RoomRepository;
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

    @Autowired
    public RoomController(RoomService roomService, @Value("${OPENVIDU_URL}")String openviduUrl, @Value("${OPENVIDU_SECRET}")String openviduSecret) {
        this.roomService = roomService;
        this.OPENVIDU_URL = openviduUrl;
        this.OPENVIDU_SECRET = openviduSecret;
        this.openvidu = new OpenVidu(openviduUrl, openviduSecret);
    }

    @PostMapping("/api/sessions")
    public ResponseEntity<String> makeRoom(@RequestBody(required = false) Map<String, Object> params)
            throws OpenViduJavaClientException, OpenViduHttpException {
        SessionProperties properties = SessionProperties.fromJson(params).build();
        Session session = openvidu.createSession(properties);
        List<Song> songs = params.get("songs") == null ? new ArrayList<>() : (List<Song>) params.get("songs");
        Long userId = params.get("userId") == null ? null : (Long) params.get("userId");
        System.out.println("userId = " + userId);
        System.out.println("songs = " + songs);
        User user = roomService.getUserById(userId);
        Room room = roomService.createRoom(session.getSessionId(), user, songs);
        return new ResponseEntity<>(session.getSessionId(), HttpStatus.OK);
    }

//    @PostMapping("/api/matchroom")
//    public ResponseEntity<String> matchRoom(@RequestBody(required = false) Map<String, Object> params)
//            throws OpenViduJavaClientException, OpenViduHttpException {
//        List<Song> songs = new ArrayList<>();
//        songs = (List<Song>) params.get("songs");
//
//        Collections.sort(songs);
//        for (int i = 0; i < rooms.size(); i++) {
//            List<Song> roomSongs = rooms.get(i).getSongs();
//            Collections.sort(roomSongs);
//            if (roomSongs.equals(songs)) {
//                Session session = openvidu.getActiveSession(rooms[i].sessionId);
//                ConnectionProperties properties = ConnectionProperties.fromJson(params).build();
//                Connection connection = session.createConnection(properties);
//                return new ResponseEntity<>(connection.getToken(), HttpStatus.OK);
//            }
//        }
//
//        SessionProperties properties = SessionProperties.fromJson(params).build();
//        Session session = openvidu.createSession(properties);
//        Long userId = (Long) params.get("userid");
//        User user = roomService.getUserById(userId);
//        Room room = Room.builder().roomId(session.getSessionId()).build();
//        roomService.createRoom(room.getRoomId(),user,songs);
//        return new ResponseEntity<>(session.getSessionId(),HttpStatus.OK);
//    }


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

    @PostMapping("/api/sessions/{sessionId}/enter")
    public ResponseEntity<String> enterRoom(@PathVariable("sessionId") String sessionId,
                                                   @RequestBody(required = false) Map<String, Object> params)
            throws OpenViduJavaClientException, OpenViduHttpException {
        Optional<Room> room = roomService.enterRoom(sessionId);
//        if(!room.isPresent()) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
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
//        Room room = roomService.findBroadcastByRoomId(sessionId);
//        roomService.goBroadcast(room);
//        return new ResponseEntity<>(connection.getToken(), HttpStatus.OK);
//    }

//    @PostMapping("/api/sessions/{sessionId}/quit")
//    public ResponseEntity<String> quitRoom(@PathVariable("sessionId") String sessionId,
//                                            @RequestBody(required = false) Map<String, Object> params, Long userId) {
//        Room room = roomService.findbyUserId(userId);
//        if (room == null) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        roomService.quitRoom(room, rooms);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
}
