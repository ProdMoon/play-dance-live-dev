package namanmu;

import com.example.manmu.controller.RoomController;
import com.example.manmu.entity.Room;
import com.example.manmu.entity.RoomDto;
import com.example.manmu.repository.PlayingRoomRepository;
import com.example.manmu.repository.WaitingRoomRepository;

import com.example.manmu.service.GameRoomService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(SpringRunner.class)
//@WebAppConfiguration
@AutoConfigureMockMvc
@SpringBootTest(classes = RoomController.class)
class RoomControllerTest {

    @InjectMocks
    private GameRoomService gameRoomService;

    @Mock
    private PlayingRoomRepository playingRoomRepository;
    @Mock
    private WaitingRoomRepository waitingRoomRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createRoom() {
        RoomDto expectedRoomDto = RoomDto.builder().build();
        when(waitingRoomRepository.getLast()).thenReturn(null);
        when(waitingRoomRepository.save(any(Room.class))).thenReturn(expectedRoomDto.toEntity());

        RoomDto actualRoomDto = gameRoomService.createRoom("userId", null);

        assertEquals(expectedRoomDto, actualRoomDto);
        verify(waitingRoomRepository, times(1)).save(any(Room.class));
    }

}