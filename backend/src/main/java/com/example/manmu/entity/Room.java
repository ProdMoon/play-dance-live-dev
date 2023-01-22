package com.example.manmu.entity;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@RedisHash("Room")
@Getter
@Setter
@NoArgsConstructor
public class Room implements Serializable {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private List<String> viewers;
    private List<String> players;
    private List<String> waiters;
    private List<String> playSongs;
    private List<Ranking> rankingList;
    private String currentChampion;
    private String currentChallenger;


    @Builder
    public Room(String roomId, List<String> viewers, List<String> players, List<String> waiters, List<String> playSongs, List<Ranking> rankingList, String currentChampion, String currentChallenger) {
        this.viewers = viewers;
        this.players = players;
        this.waiters = waiters;
        this.playSongs = playSongs;
        this.rankingList = rankingList;
        this.currentChampion = currentChampion;
        this.currentChallenger = currentChallenger;
    }
    public void addPlayer(String player) {
        lock.writeLock().lock();
        try {
            players.add(player);
        } finally {
            lock.writeLock().unlock();
        }
    }
    public void addWaiter(String waiter) {
        lock.writeLock().lock();
        try {
            waiters.add(waiter);
        } finally {
            lock.writeLock().unlock();
        }
    }
    public void addViewer(String viewer) {
        lock.writeLock().lock();
        try {
            viewers.add(viewer);
        } finally {
            lock.writeLock().unlock();
        }
    }
    public void removePlayer(String player) {
        lock.writeLock().lock();
        try {
            players.remove(player);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
