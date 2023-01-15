package com.example.manmu;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.handler.annotation.DestinationVariable;

@Setter
@Getter
public class ChatMessage {
    private MessageType type;
    private String content;
    private String sender;
    private String roomId;
    private String username;


    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

}
