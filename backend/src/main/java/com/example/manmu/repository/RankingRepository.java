package com.example.manmu.repository;

import com.example.manmu.entity.Ranking;
import com.example.manmu.entity.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface RankingRepository extends JpaRepository<Ranking, Long> {
    Ranking findByUser(User user);

    @Cacheable(value = "ranking")
    List<Ranking> findAllByOrderByBestWinNumsDesc();

    @Transactional
    @Override
    @CacheEvict(value = "ranking", allEntries = true)
    <S extends Ranking> S save(S s);

}
