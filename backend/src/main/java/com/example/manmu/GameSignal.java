package com.example.manmu;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.handler.annotation.DestinationVariable;

@Setter
@Getter
public class GameSignal {
    private String type;
    private String sender;
    private String roomId;
    private Integer currentRound;
    private String songVersion;
}
