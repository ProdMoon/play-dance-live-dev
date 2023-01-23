package com.example.manmu.service;

import com.example.manmu.entity.*;
import com.example.manmu.exception.UserNotFoundException;
import com.example.manmu.repository.RankingRepository;
import com.example.manmu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.roomRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class GameRoomService {
    private final RedisTemplate<String, Room> roomRedisTemplate;
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
        roomRedisTemplate.opsForHash().put("ROOM", "ROOM", streamRoom);
        return new RoomDto(streamRoom);
    }

    public RoomDto enterRoom(String userMail) {
        Room enterRoom = (Room) roomRedisTemplate.opsForHash().get("ROOM", "ROOM");
        if (enterRoom != null) {
            enterRoom.getViewers().add(userMail);
            roomRedisTemplate.opsForHash().put("ROOM", "ROOM", enterRoom);
            return new RoomDto(enterRoom);
        }
        return null;
    }

    @Transactional
    public RoomDto joinGame(String userMail) {
        Room joinRoom = (Room) roomRedisTemplate.opsForHash().get("ROOM", "ROOM");
        User joinUser = userRepository.findByEmail(userMail).orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + userMail));
        if (joinRoom != null && joinUser != null) {
            UserDto joinUserDto = new UserDto().builder()
                    .name(joinUser.getName())
                    .email(joinUser.getEmail())
                    .build();
            joinRoom.addWaiter(joinUserDto);
            roomRedisTemplate.opsForHash().put("ROOM", "ROOM", joinRoom);
            return new RoomDto(joinRoom);
        }
        return null;
    }

    public RoomDto endGame(String currentUserMail) {
        Room gameRoom = (Room) roomRedisTemplate.opsForHash().get("ROOM", "ROOM");
        if (currentUserMail.equals(gameRoom.getCurrentChampion())) {
            return null;
        } else {
            /*
             * leftScore = currentChampion's poll / rightScore = currentChallenger's poll
             */
            Integer leftScore = (Integer) roomRedisTemplate.opsForHash().get("POLL_LEFT", "POLL_LEFT");
            Integer rightScore = (Integer) roomRedisTemplate.opsForHash().get("POLL_RIGHT", "POLL_RIGHT");
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
                gameRoom.addPlayer(gameRoom.getWaiters().get(0).getEmail());
                gameRoom.setRankingList(rankingRepository.findAllByOrderByBestWinNumsDesc());
                currentChallengerUser.updateCurrentWinNums(currentChallengerUser.getCurrentWinNums());
                roomRedisTemplate.opsForHash().put("ROOM", "ROOM", gameRoom);
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
                gameRoom.addPlayer(gameRoom.getWaiters().get(0).getEmail());
                gameRoom.setCurrentChampion(currentChallenger);
                gameRoom.setRankingList(rankingRepository.findAllByOrderByBestWinNumsDesc());
                roomRedisTemplate.opsForHash().put("ROOM", "ROOM", gameRoom);
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
        roomRedisTemplate.opsForHash().put("WINNER", "WINNER", winner);
        roomRedisTemplate.opsForHash().put("POLL_LEFT", "POLL_LEFT", pollLeft);
        roomRedisTemplate.opsForHash().put("POLL_RIGHT", "POLL_RIGHT", pollRight);
    }

    public String findUserName(String userMail) {
        User user = userRepository.findByEmail(userMail)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + userMail));
        return user.getName();
    }

    public RoomDto startGame() {
        Room gameRoom = (Room) roomRedisTemplate.opsForHash().get("ROOM", "ROOM");
        if(gameRoom != null) {
            gameRoom.setCurrentChampion(gameRoom.getWaiters().get(0).getEmail());
            gameRoom.setCurrentChallenger(gameRoom.getWaiters().get(1).getEmail());
            gameRoom.removeWaiter(gameRoom.getWaiters().get(0));
            gameRoom.removeWaiter(gameRoom.getWaiters().get(1));
            roomRedisTemplate.opsForHash().put("ROOM", "ROOM", gameRoom);
            return new RoomDto(gameRoom);
        }
        return null;
    }
}