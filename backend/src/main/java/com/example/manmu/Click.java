package com.example.manmu;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.handler.annotation.DestinationVariable;

@Setter
@Getter
public class Click {
    private String type;
    private String value;
    private String roomId;
}
