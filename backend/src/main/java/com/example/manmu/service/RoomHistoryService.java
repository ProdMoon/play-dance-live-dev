package com.example.manmu.service;

import com.example.manmu.repository.PlayingRoomRepository;
import com.example.manmu.repository.RoomHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomHistoryService {

    private final PlayingRoomRepository playingRoomRepository;
    private final RoomHistoryRepository roomHistoryRepository;

    public void saveRoomHistory(String roomId) {
    }
}
