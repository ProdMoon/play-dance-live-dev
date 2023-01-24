package com.example.manmu.service;

import com.example.manmu.entity.*;
import com.example.manmu.exception.UserNotFoundException;
import com.example.manmu.repository.RankingRepository;
import com.example.manmu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@RequiredArgsConstructor
public class GameRoomService {
    private final RedisTemplate<String, Object> redisTemplate;
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
        roomRedisTemplate.opsForValue().set("ROOM", streamRoom);
        return new RoomDto(streamRoom);
    }

    public RoomDto enterRoom(String userMail) {
        Room enterRoom = roomRedisTemplate.opsForValue().get("ROOM");
        if (enterRoom != null) {
            enterRoom.getViewers().add(userMail);
            roomRedisTemplate.opsForValue().set("ROOM", enterRoom);
            return new RoomDto(enterRoom);
        }
        return null;
    }

    public RoomDto joinGame(String userMail, String userSong, String userConnectionId) {
        Room joinRoom = roomRedisTemplate.opsForValue().get("ROOM");
        User joinUser = userRepository.findByEmail(userMail).orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + userMail));
        if (joinRoom != null && joinUser != null) {
            UserDto joinUserDto = UserDto.builder()
                    .name(joinUser.getName())
                    .email(joinUser.getEmail())
                    .song(userSong)
                    .connectionId(userConnectionId)
                    .build();
            joinRoom.addWaiter(joinUserDto);
            // check if user already has a ranking
            joinUserMakeRanking(joinUser);

            roomRedisTemplate.opsForValue().set("ROOM", joinRoom);
            return new RoomDto(joinRoom);
        }
        return null;
    }

    private void joinUserMakeRanking(User joinUser) {
        // Try to find an existing ranking for the user
        Ranking joinUserRanking = rankingRepository.findByUser(joinUser);

        if (joinUserRanking == null) {
            // Create a new ranking if it doesn't exist
            joinUserRanking = Ranking.builder()
                    .user(joinUser)
                    .currentWinNums(0)
                    .bestWinNums(0)
                    .build();
        } else {
            // Update the existing ranking
            joinUserRanking.setCurrentWinNums(joinUserRanking.getCurrentWinNums() + 1);
            joinUserRanking.setBestWinNums(Math.max(joinUserRanking.getBestWinNums(), joinUserRanking.getCurrentWinNums()));
        }
        rankingRepository.save(joinUserRanking);
    }

    public RoomDto endGame(String currentUserMail) {
        Room gameRoom = roomRedisTemplate.opsForValue().get("ROOM");
        if (currentUserMail.equals(gameRoom.getCurrentChampion().getEmail())) {
            return null;
        } else {
            /*
             * leftScore = currentChampion's poll / rightScore = currentChallenger's poll
             */
            Integer leftScore = (Integer) redisTemplate.opsForValue().get("POLL_LEFT");
            Integer rightScore = (Integer) redisTemplate.opsForValue().get("POLL_RIGHT");
            UserDto currentChampion = gameRoom.getCurrentChampion();
            UserDto currentChallenger = gameRoom.getCurrentChallenger();
            User currentChampionUser = userRepository.findByEmail(currentChampion.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + currentChampion.getEmail()));
            User currentChallengerUser = userRepository.findByEmail(currentChallenger.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + currentChallenger.getEmail()));
            Ranking currentChampionRanking = rankingRepository.findByUser(currentChampion.toEntity());
            Ranking currentChallengerRanking = rankingRepository.findByUser(currentChallenger.toEntity());
            /*
             * champion wins
             * currentChampion과 currentChallenger의 poll을 비교하여 승수를 더해준다.
             * champion이 승리하였기 때문에 현재 승수를 +1 한다.
             * currentChallenger의 승수는 0으로 초기화한다.
             * gameRoom의 정보를 update 함(challenger remove, add new challenger, update ranking)
             */
            if (leftScore == null || rightScore == null) {
                return null;
            }
            else if (leftScore > rightScore) {
                joinUserMakeRanking(currentChampionUser);

                currentChallengerRanking.setCurrentWinNums(0);
                rankingRepository.save(currentChallengerRanking);
                List<RankingDto> rankingDtoList = rankingRepository.findAllByOrderByBestWinNumsDesc().stream().
                        map(ranking -> new RankingDto(ranking.getUser().getName(), ranking.getBestWinNums())).collect(Collectors.toList());
                gameRoom.setRankingList(rankingDtoList);

                gameRoom.removePlayer(currentChallenger);
                UserDto newChallenger = gameRoom.getWaiters().remove(0);
                gameRoom.addPlayer(newChallenger);
                gameRoom.setCurrentChallenger(newChallenger);

                redisTemplate.opsForValue().set("ROOM", gameRoom);
                return new RoomDto(gameRoom);
            }
            /*
             * challenger wins
             */
            else {
                joinUserMakeRanking(currentChallengerUser);
                List<RankingDto> rankingDtoList = rankingRepository.findAllByOrderByBestWinNumsDesc().stream().
                        map(ranking -> new RankingDto(ranking.getUser().getName(), ranking.getBestWinNums())).collect(Collectors.toList());
                gameRoom.setRankingList(rankingDtoList);

                currentChampionRanking.setCurrentWinNums(0);
                rankingRepository.save(currentChampionRanking);

                gameRoom.removePlayer(currentChampion);
                gameRoom.setCurrentChampion(currentChallenger);
                UserDto newChallenger = gameRoom.getWaiters().remove(0);
                gameRoom.addPlayer(newChallenger);
                gameRoom.setCurrentChallenger(newChallenger);

                redisTemplate.opsForValue().set("ROOM", gameRoom);
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
        redisTemplate.opsForValue().set("WINNER", winner);
        redisTemplate.opsForValue().set("POLL_LEFT", pollLeft);
        redisTemplate.opsForValue().set("POLL_RIGHT", pollRight);
    }

    public String findUserName(String userMail) {
        User user = userRepository.findByEmail(userMail)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + userMail));
        return user.getName();
    }

    public RoomDto startGame() {
        Room gameRoom = (Room) redisTemplate.opsForValue().get("ROOM");
        if(gameRoom != null) {
            UserDto startChampion = gameRoom.getWaiters().remove(0);
            UserDto startChallenger = gameRoom.getWaiters().remove(0);
            gameRoom.setCurrentChampion(startChampion);
            gameRoom.setCurrentChallenger(startChallenger);
            gameRoom.addPlayer(startChampion);
            gameRoom.addPlayer(startChallenger);
            roomRedisTemplate.opsForValue().set("ROOM", gameRoom);

            redisTemplate.opsForValue().set("POLL_LEFT", 0);
            redisTemplate.opsForValue().set("POLL_RIGHT", 0);

            return new RoomDto(gameRoom);
        }
        return null;
    }

    public UserDto findRoomUserDtoByMail(String userMail) {
        Room gameRoom = roomRedisTemplate.opsForValue().get("ROOM");
        if (gameRoom != null) {
            List<UserDto> roomUsers = gameRoom.getPlayers();
            for (UserDto userDto : roomUsers) {
                if (userDto.getEmail().equals(userMail)) {
                    return userDto;
                }
            }
        }
        return null;
    }

    public String getConnectionIdByUserMail(String userMail) {
        Room gameRoom = roomRedisTemplate.opsForValue().get("ROOM");
        if (gameRoom != null) {
            List<UserDto> roomUsers = gameRoom.getPlayers();
            for (UserDto userDto : roomUsers) {
                if (userDto.getEmail().equals(userMail)) {
                    return userDto.getConnectionId();
                }
            }
        }
        return null;
    }

    public void leaveRoom(String userMail) {
        Room gameRoom = roomRedisTemplate.opsForValue().get("ROOM");
        if (gameRoom != null) {
            List<UserDto> players = gameRoom.getPlayers();
            for (UserDto userDto : players) {
                if (userDto.getEmail().equals(userMail)) {
                    gameRoom.removePlayer(userDto);
                    roomRedisTemplate.opsForValue().set("ROOM", gameRoom);
                    return;
                }
            }
        }
    }

    public RoomDto findRoom(){
        Room gameRoom = roomRedisTemplate.opsForValue().get("ROOM");
        if(gameRoom != null){
            return new RoomDto(gameRoom);
        }
        return null;
    }

    public void voteClick(String value) {
        if (value.equals("A")) {
            Integer pollLeft = (Integer) redisTemplate.opsForValue().get("POLL_LEFT");
            if (pollLeft != null) {
                redisTemplate.opsForValue().set("POLL_LEFT", pollLeft + 1);
            }
        } else if (value.equals("B")) {
            Integer pollRight = (Integer) redisTemplate.opsForValue().get("POLL_RIGHT");
            if (pollRight != null) {
                redisTemplate.opsForValue().set("POLL_RIGHT", pollRight + 1);
            }
        }
    }
}