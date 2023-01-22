package com.example.manmu.service;

import com.example.manmu.entity.*;
import com.example.manmu.exception.UserNotFoundException;
import com.example.manmu.repository.RankingRepository;
import com.example.manmu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class GameRoomService {
    private final RedisTemplate<String, Room> redisTemplate;
    private final RankingRepository rankingRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(GameRoomService.class);

    public RoomDto createRoom() {
        Room streamRoom = Room.builder()
                .players(new ArrayList<>())
                .viewers(new ArrayList<>())
                .waiters(new ArrayList<>())
                .playSongs(new ArrayList<>())
                .rankingList(new ArrayList<>())
                .currentChampion(null)
                .build();
        redisTemplate.opsForHash().put("ROOM", 1, streamRoom);
        return new RoomDto(streamRoom);
    }

    public RoomDto enterRoom(String userMail) {
        Room enterRoom = (Room) redisTemplate.opsForHash().get("ROOM", 1);
        if (enterRoom != null) {
            enterRoom.getViewers().add(userMail);
            redisTemplate.opsForHash().put("ROOM", 1, enterRoom);
            return new RoomDto(enterRoom);
        }
        return null;
    }

    public RoomDto joinGame(String userMail) {
        Room joinRoom = (Room) redisTemplate.opsForHash().get("ROOM", 1);
        if (joinRoom != null) {
            joinRoom.addWaiter(userMail);
            redisTemplate.opsForHash().put("ROOM", 1, joinRoom);
            return new RoomDto(joinRoom);
        }
        return null;
    }

    public RoomDto endGame(String currentUserMail) {
        Room gameRoom = (Room) redisTemplate.opsForHash().get("ROOM", 1);
        if (currentUserMail.equals(gameRoom.getCurrentChampion())) {
            return null;
        } else {
            /*
             * leftScore = currentChampion's poll / rightScore = currentChallenger's poll
             */
            Integer leftScore = (Integer) redisTemplate.opsForHash().get("POLL_LEFT", "POLL_LEFT");
            Integer rightScore = (Integer) redisTemplate.opsForHash().get("POLL_RIGHT", "POLL_RIGHT");
            String currentChampion = gameRoom.getCurrentChampion();
            String currentChallenger = gameRoom.getPlayers().get(1);
            User currentChampionUser = userRepository.findByEmail(currentChampion)
                    .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + currentChampion));
            User currentChallengerUser = userRepository.findByEmail(currentChallenger)
                    .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + currentChallenger));
            /*
             * champion wins
             * currentChampionr과 currentChallenger의 poll을 비교하여 승수를 더해준다.
             * champion이 승리하였기 때문에 현재 승수를 +1 한다.
             * currentChallenger의 승수는 0으로 초기화한다.
             * gameRoom의 정보를 update 함(challenger remove, add new challenger, update ranking)
             */
            if (leftScore > rightScore) {
                Ranking currentChampionRanking = rankingRepository.findByUserEmail(currentChampion);
                Ranking currentChallengerRanking = rankingRepository.findByUserEmail(currentChallenger);
                currentChampionRanking.setCurrentWinNums(currentChampionRanking.getCurrentWinNums() + 1);
                if (currentChampionRanking.getCurrentWinNums() > currentChampionRanking.getBestWinNums()) {
                    currentChampionRanking.setBestWinNums(currentChampionRanking.getCurrentWinNums());
                }
                currentChallengerRanking.setCurrentWinNums(0);
                rankingRepository.save(currentChampionRanking);
                rankingRepository.save(currentChallengerRanking);
                gameRoom.removePlayer(currentChallenger);
                gameRoom.addPlayer(gameRoom.getWaiters().get(0));
                gameRoom.setRankingList(rankingRepository.findAllByOrderByBestWinNumsDesc());
                currentChallengerUser.updateCurrentWinNums(currentChallengerUser.getCurrentWinNums());
                redisTemplate.opsForHash().put("ROOM", 1, gameRoom);
                return new RoomDto(gameRoom);
            }
            /*
             * challenger wins
             */
            else {
                Ranking currentChampionRanking = rankingRepository.findByUserEmail(currentChampion);
                Ranking currentChallengerRanking = rankingRepository.findByUserEmail(currentChallenger);
                currentChallengerRanking.setCurrentWinNums(currentChallengerRanking.getCurrentWinNums() + 1);
                if (currentChallengerRanking.getCurrentWinNums() > currentChallengerRanking.getBestWinNums()) {
                    currentChallengerRanking.setBestWinNums(currentChallengerRanking.getCurrentWinNums());
                }
                currentChampionRanking.setCurrentWinNums(0);
                rankingRepository.save(currentChallengerRanking);
                gameRoom.removePlayer(currentChampion);
                gameRoom.addPlayer(gameRoom.getWaiters().get(0));
                gameRoom.setCurrentChampion(currentChallenger);
                gameRoom.setRankingList(rankingRepository.findAllByOrderByBestWinNumsDesc());
                redisTemplate.opsForHash().put("ROOM", 1, gameRoom);
                return new RoomDto(gameRoom);
            }
        }
    }
    public void vote (VoteData voteData){
        String type = voteData.getType();
        String sender = voteData.getSender();
        String winner = voteData.getWinner();
        Integer pollLeft = voteData.getPollLeft();
        Integer pollRight = voteData.getPollRight();
        redisTemplate.opsForHash().put("WINNER", "WINNER", winner);
        redisTemplate.opsForHash().put("POLL_LEFT", "POLL_LEFT", pollLeft);
        redisTemplate.opsForHash().put("POLL_RIGHT", "POLL_RIGHT", pollRight);
    }
}