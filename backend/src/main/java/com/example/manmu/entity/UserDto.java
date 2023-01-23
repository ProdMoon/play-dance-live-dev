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

    @Builder
    public UserDto(String name, String email, String picture, Integer currentWinNums, Integer bestWinNums) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.currentWinNums = currentWinNums;
        this.bestWinNums = bestWinNums;
    }

    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .currentWinNums(currentWinNums)
                .bestWinNums(bestWinNums)
                .build();
    }
}
