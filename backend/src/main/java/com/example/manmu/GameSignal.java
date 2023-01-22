package com.example.manmu;
import com.example.manmu.entity.Ranking;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.handler.annotation.DestinationVariable;

import java.util.List;

@Setter
@Getter
public class GameSignal {
    private String type;
    private String sender;
    private List<String> waiters;
    private String winner;
    private String challenger;
    private List<Ranking> rankingList;
    private Integer currentRound;
    private String connectionId;
}
