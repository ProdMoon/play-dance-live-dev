package com.example.manmu.entity;

import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ranking")
public class Ranking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ColumnDefault("0")
    private Integer currentWinNums;

    @ColumnDefault("0")
    private Integer bestWinNums;

    @Builder
    public Ranking(User user, Integer currentWinNums, Integer bestWinNums) {
        this.user = user;
        this.currentWinNums = currentWinNums;
        this.bestWinNums = bestWinNums;
    }
}
