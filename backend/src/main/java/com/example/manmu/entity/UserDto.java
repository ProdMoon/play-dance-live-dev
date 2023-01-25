package com.example.manmu.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String picture;
    private Integer currentWinNums;
    private Integer bestWinNums;
    private String song;
    private String connectionId;

    @Builder
    public UserDto(String name, String email, String picture, Integer currentWinNums, Integer bestWinNums, String song , String connectionId) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.currentWinNums = currentWinNums;
        this.bestWinNums = bestWinNums;
        this.song = song;
        this.connectionId = connectionId;

    }

    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .build();
    }
}
