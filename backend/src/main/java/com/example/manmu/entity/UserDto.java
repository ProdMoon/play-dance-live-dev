package com.example.manmu.entity;

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

    public User toEntity() {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .picture(picture)
                .currentWinNums(currentWinNums)
                .bestWinNums(bestWinNums)
                .build();
    }
}
