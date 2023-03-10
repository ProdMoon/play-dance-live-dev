package com.example.manmu.repository;

import com.example.manmu.entity.Ranking;
import com.example.manmu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankingRepository extends JpaRepository<Ranking, Long> {
    Ranking findByUser(User user);
    List<Ranking> findAllByOrderByBestWinNumsDesc();
}
