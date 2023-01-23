package com.example.manmu;
import com.example.manmu.entity.Ranking;
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
    private String Champion;
    private String challenger;
    private String userName;
    private List<Ranking> rankingList;
    private String connectionId;
}
