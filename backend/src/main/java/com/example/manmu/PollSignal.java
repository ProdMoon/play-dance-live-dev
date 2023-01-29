package com.example.manmu;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class PollSignal {
    private Integer championPoll;
    private Integer challengerPoll;

    @Builder
    public PollSignal(Integer championPoll, Integer challengerPoll) {
        this.championPoll = championPoll;
        this.challengerPoll = challengerPoll;
    }
}
