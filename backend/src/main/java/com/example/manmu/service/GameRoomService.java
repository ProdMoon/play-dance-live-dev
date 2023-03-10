package com.example.manmu.service;

import com.example.manmu.PollSignal;
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


@Service
@RequiredArgsConstructor
public class GameRoomService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Room> roomRedisTemplate;
    private final RankingRepository rankingRepository;
    private final UserRepository userRepository;

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
        User joinUser = userRepository.findByEmail(userMail)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + userMail));
        if (joinRoom != null && joinUser != null) {
            UserDto joinUserDto = UserDto.builder()
                    .name(joinUser.getName())
                    .email(joinUser.getEmail())
                    .song(userSong)
                    .connectionId(userConnectionId)
                    .build();
            joinRoom.addWaiter(joinUserDto);
            joinRoom.addViewer(userMail);
            // check if user already has a ranking
            makeJoinUserRanking(joinUser);
            List<RankingDto> rankingDtoList = rankingRepository.findAllByOrderByBestWinNumsDesc().stream().
                    map(ranking -> new RankingDto(ranking.getUser().getName(), ranking.getBestWinNums())).collect(Collectors.toList());
            joinRoom.setRankingList(rankingDtoList);
            roomRedisTemplate.opsForValue().set("ROOM", joinRoom);
            return new RoomDto(joinRoom);
        }
        return null;
    }

    public RoomDto enterGame(String userMail){
        Room enterRoom = roomRedisTemplate.opsForValue().get("ROOM");
        User enterUser = userRepository.findByEmail(userMail)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + userMail));
        if (enterRoom != null && enterUser != null) {
            enterRoom.addViewer(userMail);
            roomRedisTemplate.opsForValue().set("ROOM", enterRoom);
            return new RoomDto(enterRoom);
        }
        return null;
    }

    public PollSignal getCurrentPoll() {
        Integer leftScore = (Integer) redisTemplate.opsForValue().get("POLL_LEFT");
        Integer rightScore = (Integer) redisTemplate.opsForValue().get("POLL_RIGHT");
        PollSignal pollSignal = PollSignal.builder()
                .championPoll(rightScore)
                .challengerPoll(leftScore)
                .build();

        return pollSignal;
    }

    private void makeJoinUserRanking(User joinUser) {
        // Try to find an existing ranking for the user
        Ranking joinUserRanking = rankingRepository.findByUser(joinUser);

        if (joinUserRanking == null) {
            // Create a new ranking if it doesn't exist
            joinUserRanking = Ranking.builder()
                    .user(joinUser)
                    .currentWinNums(0)
                    .bestWinNums(0)
                    .build();
            rankingRepository.save(joinUserRanking);
        }
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
            UserDto currentChampionDto = gameRoom.getCurrentChampion();
            UserDto currentChallengerDto = gameRoom.getCurrentChallenger();
            User currentChampionUser = userRepository.findByEmail(currentChampionDto.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + currentChampionDto.getEmail()));
            User currentChallengerUser = userRepository.findByEmail(currentChallengerDto.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + currentChallengerDto.getEmail()));
            Ranking currentChampionRanking = rankingRepository.findByUser(currentChampionUser);
            Ranking currentChallengerRanking = rankingRepository.findByUser(currentChallengerUser);
            
            if (leftScore == null || rightScore == null) {
                return null;
            }
            /*
             * champion wins
             * currentChampion과 currentChallenger의 poll을 비교하여 승수를 더해준다.
             * champion이 승리하였기 때문에 현재 승수를 +1 한다.
             * currentChallenger의 승수는 0으로 초기화한다.
             * gameRoom의 정보를 update 함(challenger remove, add new challenger, update ranking)
             */
            else if (leftScore > rightScore) {
                System.out.println("before" + currentChampionDto.getCurrentWinNums());
                updateWinnerRanking(gameRoom, currentChampionDto, currentChampionRanking);
                gameRoom.setCurrentChampion(currentChampionDto);
                System.out.println("after" + currentChampionDto.getCurrentWinNums());
                updateLoserRanking(gameRoom, currentChallengerDto, currentChallengerRanking);

                UserDto newChallenger = getNewChallenger(gameRoom);
                gameRoom.addPlayer(newChallenger);
                gameRoom.setCurrentChallenger(newChallenger);
                
                redisTemplate.opsForValue().set("ROOM", gameRoom);
                return new RoomDto(gameRoom);
            }
            /*
             * challenger wins
             */
            else {
                updateWinnerRanking(gameRoom, currentChallengerDto, currentChallengerRanking);
                updateLoserRanking(gameRoom, currentChampionDto, currentChampionRanking);
                gameRoom.setCurrentChampion(currentChallengerDto);


                UserDto newChallenger = getNewChallenger(gameRoom);
                gameRoom.addPlayer(newChallenger);
                gameRoom.setCurrentChallenger(newChallenger);

                redisTemplate.opsForValue().set("ROOM", gameRoom);
                return new RoomDto(gameRoom);
            }
        }
    }

    private UserDto getNewChallenger(Room gameRoom) {
        UserDto newChallenger = gameRoom.getWaiters().remove(0);
        User newChallengerUser = userRepository.findByEmail(newChallenger.getEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + newChallenger.getEmail()));
        Ranking newChallengerRanking = rankingRepository.findByUser(newChallengerUser);
        newChallenger.setCurrentWinNums(newChallengerRanking.getCurrentWinNums());
        newChallenger.setBestWinNums(newChallengerRanking.getBestWinNums());
        return newChallenger;
    }

    private void updateWinnerRanking(Room gameRoom, UserDto currentWinnerDto, Ranking currentWinnerRanking) {
        currentWinnerRanking.setCurrentWinNums(currentWinnerRanking.getCurrentWinNums() + 1);
        currentWinnerRanking.setBestWinNums(Math.max(currentWinnerRanking.getBestWinNums(), currentWinnerRanking.getCurrentWinNums()));
        currentWinnerDto.setCurrentWinNums(currentWinnerRanking.getCurrentWinNums());
        currentWinnerDto.setBestWinNums(currentWinnerRanking.getBestWinNums());
        // Update the existing ranking
        List<RankingDto> rankingDtoList = rankingRepository.findAllByOrderByBestWinNumsDesc().stream().
                map(ranking -> new RankingDto(ranking.getUser().getName(), ranking.getBestWinNums())).collect(Collectors.toList());
        gameRoom.setRankingList(rankingDtoList);

        rankingRepository.save(currentWinnerRanking);
    }
    private void updateLoserRanking(Room gameRoom, UserDto currentLoserDto, Ranking currentLoserRanking) {
        currentLoserRanking.setCurrentWinNums(0);
        rankingRepository.save(currentLoserRanking);

        List<RankingDto> rankingDtoList = rankingRepository.findAllByOrderByBestWinNumsDesc().stream().
                map(ranking -> new RankingDto(ranking.getUser().getName(), ranking.getBestWinNums())).collect(Collectors.toList());
        gameRoom.setRankingList(rankingDtoList);

        currentLoserDto.setCurrentWinNums(0);
        currentLoserDto.setBestWinNums(currentLoserRanking.getBestWinNums());
        gameRoom.removePlayer(currentLoserDto);
    }

    //    private void updateLoserRankingAndChallenger(User currentLoser, Room gameRoom) {
//        Ranking currentLoserRanking = rankingRepository.findByUser(currentLoser);
//
//        currentLoserRanking.setCurrentWinNums(0);
//        rankingRepository.save(currentLoserRanking);
//
//        List<RankingDto> rankingDtoList = rankingRepository.findAllByOrderByBestWinNumsDesc().stream().
//                map(ranking -> new RankingDto(ranking.getUser().getName(), ranking.getBestWinNums())).collect(Collectors.toList());
//        gameRoom.setRankingList(rankingDtoList);
//
//        gameRoom.removePlayer(gameRoo);
//        UserDto newChallenger = gameRoom.getWaiters().remove(0);
//        gameRoom.addPlayer(newChallenger);
//        gameRoom.setCurrentChallenger(newChallenger);
//    }
    public void vote (VoteData voteData){
        Integer pollLeft = voteData.getPollLeft();
        Integer pollRight = voteData.getPollRight();
        redisTemplate.opsForValue().set("POLL_LEFT", pollLeft);
        redisTemplate.opsForValue().set("POLL_RIGHT", pollRight);
    }

    public RoomDto startGame() {
        Room gameRoom = (Room) redisTemplate.opsForValue().get("ROOM");
        if(gameRoom != null) {
            UserDto startChampionDto = gameRoom.getWaiters().remove(0);
            UserDto startChallengerDto = gameRoom.getWaiters().remove(0);
            setDtoWinNums(gameRoom, startChampionDto, startChallengerDto);
            roomRedisTemplate.opsForValue().set("ROOM", gameRoom);
            redisTemplate.opsForValue().set("POLL_LEFT", 0);
            redisTemplate.opsForValue().set("POLL_RIGHT", 0);
            return new RoomDto(gameRoom);
        }
        return null;
    }

    private void setDtoWinNums(Room gameRoom, UserDto startChampionDto, UserDto startChallengerDto) {
        User startChampionUser = userRepository.findByEmail(startChampionDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + startChampionDto.getEmail()));
        User startChallengerUser = userRepository.findByEmail(startChallengerDto.getEmail())
                        .orElseThrow(()-> new UserNotFoundException("해당 유저를 찾을 수 없습니다! " + startChallengerDto.getEmail()));
        Ranking startChampionRanking = rankingRepository.findByUser(startChampionUser);
        Ranking startChallengerRanking = rankingRepository.findByUser(startChallengerUser);

        startChampionDto.setCurrentWinNums(startChampionRanking.getCurrentWinNums());
        startChampionDto.setBestWinNums(startChampionRanking.getBestWinNums());
        startChallengerDto.setCurrentWinNums(startChallengerRanking.getCurrentWinNums());
        startChallengerDto.setBestWinNums(startChallengerRanking.getBestWinNums());

        gameRoom.setCurrentChampion(startChampionDto);
        gameRoom.setCurrentChallenger(startChallengerDto);
        gameRoom.addPlayer(startChampionDto);
        gameRoom.addPlayer(startChallengerDto);
    }

    public UserDto findPlayerUserDtoByMail(String userMail) {
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

    public void setCurrentDancer(String userConnectionId) {
        Room gameRoom = roomRedisTemplate.opsForValue().get("ROOM");
        if (gameRoom != null) {
            gameRoom.setCurrentDancerConnectionId(userConnectionId);
            roomRedisTemplate.opsForValue().set("ROOM", gameRoom);
        }
    }

    public RoomDto changeSong(String userMail, String userSong) {
        Room gameRoom = roomRedisTemplate.opsForValue().get("ROOM");
        if (gameRoom != null) {
            UserDto championDto = gameRoom.getCurrentChampion();
            UserDto challengerDto = gameRoom.getCurrentChallenger();
            if(championDto.getEmail().equals(userMail)){
                championDto.setSong(userSong);
                gameRoom.setCurrentChampion(championDto);
            }else if(challengerDto.getEmail().equals(userMail)){
                challengerDto.setSong(userSong);
                gameRoom.setCurrentChallenger(challengerDto);
            }
            roomRedisTemplate.opsForValue().set("ROOM", gameRoom);
            return new RoomDto(gameRoom);
        }
        return null;
    }

    public void resetPoll() {
        redisTemplate.opsForValue().set("POLL_LEFT", 0);
        redisTemplate.opsForValue().set("POLL_RIGHT", 0);
    }
}