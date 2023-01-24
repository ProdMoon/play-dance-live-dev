package com.example.manmu.entity;

import com.example.manmu.domain.user.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;

@Getter
@NoArgsConstructor
@Table(name= "user")
@Entity
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ColumnDefault("0")
    private Integer currentWinNums;

    @ColumnDefault("0")
    private Integer bestWinNums;

    @Builder
    public User(Long id, String name, String email, String picture, Role role, Integer currentWinNums, Integer bestWinNums) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
        this.currentWinNums = currentWinNums;
        this.bestWinNums = bestWinNums;
    }

    public User(String name, String email, String picture, Role role, Integer currentWinNums, Integer bestWinNums) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
        this.currentWinNums = currentWinNums;
        this.bestWinNums = bestWinNums;
    }

    public User update(String name, String picture) {
        this.name = name;
        this.picture = picture;

        return this;
    }

    public Integer updateCurrentWinNums(Integer currentWinNums) {
        this.currentWinNums = currentWinNums;
        return this.currentWinNums;
    }

    public Integer updateBestWinNums(Integer bestWinNums) {
        this.bestWinNums = bestWinNums;
        return this.bestWinNums;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}
