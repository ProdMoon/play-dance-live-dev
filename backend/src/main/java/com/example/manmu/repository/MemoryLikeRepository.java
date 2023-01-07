package com.example.manmu.repository;

import org.springframework.stereotype.Repository;

import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class MemoryLikeRepository implements LikeRepository{
    private final AtomicInteger likeCount = new AtomicInteger();

    public MemoryLikeRepository() {
        likeCount.set(0);
    }

    @Override
    public int addLike() {
        return likeCount.incrementAndGet();
    }

    @Override
    public int getLike() {
        return likeCount.get();
    }
}