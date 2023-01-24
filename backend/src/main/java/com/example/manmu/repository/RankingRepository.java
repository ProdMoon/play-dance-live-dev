package com.example.manmu.repository;

import com.example.manmu.entity.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankingRepository extends JpaRepository<Ranking, Long> {
    public Ranking findByUserEmail(String userEmail);
    public Ranking findByUserName(String userName);
    public List<Ranking> findAllByOrderByBestWinNumsDesc();

}
