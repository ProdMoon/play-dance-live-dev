package com.example.manmu.service;

import com.example.manmu.entity.*;
import com.example.manmu.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class GameRoomService {
    private final PlayingRoomRepository playingRoomRepository;
    private final WaitingRoomRepository waitingRoomRepository;
    private static final Logger logger = LoggerFactory.getLogger(GameRoomService.class);

    public RoomDto createRoom(String userId, List<String> songs) {
        Room lastRoom = waitingRoomRepository.getLast();
        RoomDto roomDto = RoomDto.builder()
                .roomId(UUID.randomUUID().toString())
                .songs(songs)
                .roomOwner(userId)
                .users(new ArrayList<>())
                .prev(lastRoom == null ? null : lastRoom.getRoomId())
                .isEmpty(false)
                .build();
        roomDto.getUsers().add(userId);
        if (lastRoom != null) {
            lastRoom.setNext(roomDto.getRoomId());
            waitingRoomRepository.updateRoom(lastRoom);
        }
        waitingRoomRepository.save(roomDto.toEntity());
        return roomDto;
    }

    public RoomDto matchRoom(List<String> songs, String userId) {
        List<Room> rooms = (List<Room>) waitingRoomRepository.findAll();
        for (Room matchRoom : rooms) {
            List<String> roomSongsCopy = (List<String>) matchRoom.getSongs();
            roomSongsCopy.retainAll(songs);
            if (roomSongsCopy.size() == songs.size()) {
                matchRoom.getUsers().add(userId);
                waitingRoomRepository.updateRoom(matchRoom);
                return new RoomDto(matchRoom);
                }
            }
        return createRoom(userId, songs);
    }

    public RoomDto enterRoom(String roomId, String userId, String direction) {
        if ((userId == null) && (roomId.equals("default") && direction.equals("current"))) {
            Room enterRoom = playingRoomRepository.getFirst();
            if (enterRoom != null) {
                return new RoomDto(enterRoom);
            }
        }
        if ((userId != null) && (roomId.equals("default") && direction.equals("current"))) {
            Room room = playingRoomRepository.getFirst();
            if (room != null) {
                room.getUsers().add(userId);
                playingRoomRepository.updateRoom(room);
                return new RoomDto(room);
            }
        }
        if ((userId != null) && direction.equals("current")) {
            Room foundRoom = playingRoomRepository.findById(roomId).orElse(null);
            if (foundRoom != null) {
                foundRoom.getUsers().add(userId);
                playingRoomRepository.updateRoom(foundRoom);
                return new RoomDto(foundRoom);
            }
        }
        if (direction.equals("next")) {
            Room currentRoom = playingRoomRepository.findById(roomId).orElse(null);
            if (currentRoom != null) {
                Room nextRoom = playingRoomRepository.findById(currentRoom.getNext()).orElse(null);
                if (nextRoom != null) {
                    currentRoom.getUsers().remove(userId);
                    playingRoomRepository.updateRoom(currentRoom);
                    nextRoom.getUsers().add(userId);
                    playingRoomRepository.updateRoom(nextRoom);
                    return new RoomDto(nextRoom);
                }
                Room firstRoom = playingRoomRepository.getFirst();
                if (firstRoom != null) {
                    currentRoom.getUsers().remove(userId);
                    playingRoomRepository.updateRoom(currentRoom);
                    firstRoom.getUsers().add(userId);
                    playingRoomRepository.updateRoom(firstRoom);
                    return new RoomDto(firstRoom);
                }
            }
            return null;
        }
        if (direction.equals("prev")) {
            Room currentRoom = playingRoomRepository.findById(roomId).orElse(null);
            if (currentRoom != null){
                Room prevRoom = playingRoomRepository.findById(currentRoom.getPrev()).orElse(null);
                if (prevRoom != null) {
                    currentRoom.getUsers().remove(userId);
                    playingRoomRepository.updateRoom(currentRoom);
                    prevRoom.getUsers().add(userId);
                    playingRoomRepository.updateRoom(prevRoom);
                    return new RoomDto(prevRoom);
                }
                Room lastRoom = playingRoomRepository.getLast();
                if (lastRoom != null) {
                    currentRoom.getUsers().remove(userId);
                    playingRoomRepository.updateRoom(currentRoom);
                    lastRoom.getUsers().add(userId);
                    playingRoomRepository.updateRoom(lastRoom);
                    return new RoomDto(lastRoom);
                }
            }
        }
        return null;
    }

    public List<RoomDto> getWaitingRooms(){
        List<Room> rooms = (List<Room>) waitingRoomRepository.findAll();
        List<RoomDto> roomDtos = new ArrayList<>();
        for (Room room : rooms) {
            roomDtos.add(new RoomDto(room));
        }
        return roomDtos;
    }

    public List<RoomDto> getPlayingRooms(){
        List<Room> rooms = (List<Room>) playingRoomRepository.findAll();
        List<RoomDto> roomDtos = new ArrayList<>();
        for (Room room : rooms) {
            roomDtos.add(new RoomDto(room));
        }
        return roomDtos;
    }

    public RoomDto startPlaying(String roomId) {
        Room currentRoom = waitingRoomRepository.findById(roomId).orElse(null);
        if (currentRoom != null) {
            Room prevRoom = waitingRoomRepository.findById(currentRoom.getPrev()).orElse(null);
            Room nextRoom = waitingRoomRepository.findById(currentRoom.getNext()).orElse(null);
            if (prevRoom != null) {
                prevRoom.setNext(currentRoom.getNext());
                waitingRoomRepository.updateRoom(prevRoom);
            }
            if (nextRoom != null) {
                nextRoom.setPrev(currentRoom.getPrev());
                waitingRoomRepository.updateRoom(nextRoom);
            }
            waitingRoomRepository.delete(currentRoom);

            Room lastPlayingRoom = playingRoomRepository.getLast();
            if(lastPlayingRoom != null) {
                lastPlayingRoom.setNext(currentRoom.getRoomId());
                currentRoom.setPrev(lastPlayingRoom.getRoomId());
                playingRoomRepository.updateRoom(lastPlayingRoom);
            }
            currentRoom.setNext(null);
            playingRoomRepository.save(currentRoom);

            return new RoomDto(currentRoom);
        }
        return null;
    }

    public void endPlaying(String roomId) {
        Room exitRoom = playingRoomRepository.findById(roomId).orElse(null);
        if (exitRoom != null){
            Room prevRoom = playingRoomRepository.findById(exitRoom.getPrev()).orElse(null);
            Room nextRoom = playingRoomRepository.findById(exitRoom.getNext()).orElse(null);
            prevRoom.setNext(exitRoom.getNext());
            playingRoomRepository.updateRoom(prevRoom);
            nextRoom.setPrev(exitRoom.getPrev());
            playingRoomRepository.updateRoom(nextRoom);
            playingRoomRepository.delete(exitRoom);
        }
    }

    public void leaveRoom(String roomId, String userId) {
        Room leaveRoom = playingRoomRepository.findById(roomId).orElse(null);
        if (leaveRoom != null) {
            leaveRoom.getUsers().remove(userId);
            if (leaveRoom.getUsers().size() == 0) {
                endPlaying(roomId);
                return;
            }
            if(leaveRoom.getRoomOwner().equals(userId)) {
                endPlaying(roomId);
            }
        }
    }
}
