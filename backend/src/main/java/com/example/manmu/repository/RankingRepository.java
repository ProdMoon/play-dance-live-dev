package com.example.manmu.repository;

import com.example.manmu.entity.Ranking;
import com.example.manmu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankingRepository extends JpaRepository<Ranking, Long> {

    public Ranking findByUser(User user);

    public List<Ranking> findAllByOrderByUser_BestWinNumsDesc();
}

//    public List<Ranking> findAllByOrderByBestWinNumsDesc();
//    public Ranking findByUserEmail(String userEmail);
//    public Ranking findByUserName(String userName);