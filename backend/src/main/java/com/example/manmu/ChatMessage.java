package com.example.manmu;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatMessage {
    private MessageType type;
    private String content;
    private String sender;
    private String roomId;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

}
