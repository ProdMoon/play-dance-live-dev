package com.example.manmu;

import com.example.manmu.entity.*;
import com.example.manmu.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RoomService {
    private final PlayingRoomRepository playingRoomRepository;
    private final WaitingRoomRepository waitingRoomRepository;

    public RoomDto createRoom(String userId, List<Song> songs) {
        RoomDto roomDto = RoomDto.builder()
                .roomId(UUID.randomUUID().toString())
                .songs(songs)
                .users(new ArrayList<>())
                .prev(waitingRoomRepository.getLast())
                .isEmpty(false)
                .build();
        roomDto.getUsers().add(userId);
        if(roomDto.getPrev() != null) {
            Room prevRoom = waitingRoomRepository.getLast();
            prevRoom.setNext(roomDto.toEntity());
        }
        waitingRoomRepository.save(roomDto.toEntity());
        return roomDto;
    }

    public RoomDto matchRoom(List<Song> songs, String userId) {
        // TODO: implement enterRoom method when user enters room.
        List<Room> rooms = (List<Room>) waitingRoomRepository.findAll();
        for (Room room : rooms) {
            List<Song> roomSongsCopy = new ArrayList<>(room.getSongs());
            roomSongsCopy.retainAll(songs);
            if (roomSongsCopy.size() == songs.size()) {
                return new RoomDto(room);
                }
            }
        return createRoom(userId, songs);
    }

    public RoomDto enterRoom(String roomId, String userId, String direction) {
        if ((userId == null) && (roomId.equals("default"))) {
            Room room =  waitingRoomRepository.getFirst();
            if (room != null) {
                return new RoomDto(room);
            }
        }
        if((userId != null) && (roomId.equals("default"))) {
            Room room =  waitingRoomRepository.getFirst();
            if (room != null) {
                room.getUsers().add(userId);
                waitingRoomRepository.save(room);
                return new RoomDto(room);
            }
        }
        if (direction.equals("current")) {
            Room foundRoom = playingRoomRepository.findById(roomId).get();
            if (foundRoom != null) {
                return new RoomDto(foundRoom);
            }
        } else if (direction.equals("next")) {
            Room currentRoom = playingRoomRepository.findById(roomId).get();
            Room nextRoom = currentRoom.getNext();
            currentRoom.getUsers().remove(userId);
            nextRoom.getUsers().add(userId);
            return new RoomDto(nextRoom);
        } else if (direction.equals("prev")) {
            Room currentRoom = playingRoomRepository.findById(roomId).get();
            Room prevRoom = currentRoom.getPrev();
            currentRoom.getUsers().remove(userId);
            prevRoom.getUsers().add(userId);
            return new RoomDto(prevRoom);
        }
        Room room =  waitingRoomRepository.getFirst();
        if (room != null) {
            return new RoomDto(room);
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
        List<Room> rooms = (List<Room>) waitingRoomRepository.findAll();
        List<RoomDto> roomDtos = new ArrayList<>();
        for (Room room : rooms) {
            roomDtos.add(new RoomDto(room));
        }
        return roomDtos;
    }

    public RoomDto startPlaying(String roomId) {
        Room room = waitingRoomRepository.findById(roomId).get();
        waitingRoomRepository.delete(room);
        room.getPrev().setNext(room.getNext());
        room.getNext().setPrev(room.getPrev());
        playingRoomRepository.save(room);
        room.setPrev(playingRoomRepository.getLast());
        room.getPrev().setNext(room);
        return new RoomDto(room);
    }

    public void exitPlaying(String roomId) {
        Room exitRoom = playingRoomRepository.findById(roomId).get();
        exitRoom.getPrev().setNext(exitRoom.getNext());
        exitRoom.getNext().setPrev(exitRoom.getPrev());
        playingRoomRepository.delete(exitRoom);
    }
}
