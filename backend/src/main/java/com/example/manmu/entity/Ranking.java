package com.example.manmu.entity;

import lombok.*;
import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "ranking")
public class Ranking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Builder
    public Ranking(Long id, User user) {
        this.id = id;
        this.user = user;
    }
}

//import lombok.*;
//import org.hibernate.annotations.ColumnDefault;
//
//import javax.persistence.*;

//@Getter
//@Setter
//@Entity
//@AllArgsConstructor
//@NoArgsConstructor
//public class Ranking {
//    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE)
//    @Column(nullable = false)
//    private Long id;
//    private String userName;
//    private String userEmail;
//    private int bestWinNums;
//    private int currentWinNums;
//
//    @Builder
//    public Ranking(String userName, String userEmail, int bestWinNums, int currentWinNums) {
//        this.userName = userName;
//        this.userEmail = userEmail;
//        this.bestWinNums = bestWinNums;
//        this.currentWinNums = currentWinNums;
//    }
//}
