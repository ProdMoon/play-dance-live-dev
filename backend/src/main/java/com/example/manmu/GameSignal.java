package com.example.manmu;
import com.example.manmu.entity.Ranking;
import com.example.manmu.entity.RankingDto;
import com.example.manmu.entity.UserDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.handler.annotation.DestinationVariable;

import java.util.List;

@Setter
@Getter
public class GameSignal {
    private String type;
    private String sender;
    private List<UserDto> waiters;
    private UserDto champion;
    private UserDto challenger;
    private String userName;
    private String song;
    private List<RankingDto> rankingList;
    private String connectionId;
}
