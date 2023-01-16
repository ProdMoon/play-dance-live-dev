package com.example.manmu.repository;

import com.example.manmu.entity.Room;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class PlayingRoomRepository implements CrudRepository<Room, String> {

    private final RedisTemplate<String, Room> redisTemplate;

    private final String KEY = "PlayingRooms";

    @Override
    public <S extends Room> S save(S room) {
        redisTemplate.opsForList().rightPush(KEY, room);
        return room;
    }

    @Override
    public <S extends Room> Iterable<S> saveAll(Iterable<S> rooms) {
        redisTemplate.opsForList().rightPushAll(KEY, (Room) rooms);
        return rooms;
    }

    @Override
    public Optional<Room> findById(String roomId) {
        List<Room> rooms = redisTemplate.opsForList().range(KEY, 0, -1);
        if (rooms != null) {
            for (Room room : rooms) {
                if (room.getRoomId().equals(roomId)) {
                    return Optional.of(room);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(String roomId) {
        List<Room> rooms = redisTemplate.opsForList().range(KEY, 0, -1);
        if(rooms == null) {
            return false;
        }
        for (Room room : rooms) {
            if (room.getRoomId().equals(roomId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterable<Room> findAll() {
        return redisTemplate.opsForList().range(KEY, 0, -1);
    }

    @Override
    public Iterable<Room> findAllById(Iterable<String> roomIds) {
        List<Room> rooms = new ArrayList<>();
        redisTemplate.opsForHash().values(KEY).forEach(val -> {
            Room room = (Room) val;
            for (String id : roomIds) {
                if (room.getRoomId().equals(id)) {
                    rooms.add(room);
                }
            }
        });
        return rooms;
    }

    @Override
    public long count() {
        List<Room> rooms = redisTemplate.opsForList().range(KEY, 0, -1);
        if(rooms == null) {
            return 0;
        }
        return rooms.size();
    }

    @Override
    public void deleteById(String id) {
        redisTemplate.opsForList().remove(KEY, 1, id);
    }

    @Override
    public void delete(Room room) {
        redisTemplate.opsForList().remove(KEY, 1, room);
    }

    @Override
    public void deleteAllById(Iterable<? extends String> roomIds) {
        roomIds.forEach(id -> redisTemplate.opsForList().remove(KEY, 1, id));
    }

    @Override
    public void deleteAll(Iterable<? extends Room> rooms) {
        rooms.forEach(room -> redisTemplate.opsForList().remove(KEY, 1, room));
    }

    @Override
    public void deleteAll() {
        redisTemplate.delete(KEY);
    }

    public int findIndex(String roomId) {
        List<Room> rooms = redisTemplate.opsForList().range(KEY, 0, -1);
        if (rooms != null) {
            for (int i = 0; i < rooms.size(); i++) {
                if (rooms.get(i).getRoomId().equals(roomId)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void updateRoom(Room room) {
        int index = findIndex(room.getRoomId());
        if (index != -1) {
            redisTemplate.opsForList().set(KEY, index, room);
        }
    }

    public Room getLast() {
        Long size = redisTemplate.opsForList().size(KEY);
        if(size == null) {
            return null;
        }
        return redisTemplate.opsForList().index(KEY, size - 1);
    }
    public Room getFirst() {
        return redisTemplate.opsForList().index(KEY, 0);
    }
}
