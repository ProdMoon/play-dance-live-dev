package com.example.manmu.repository;

import com.example.manmu.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.synchronizedList;

@Repository
public class WaitMemberRepository implements MemberRepository{

    private List<Member> readyQueue;

    public WaitMemberRepository() {
        this.readyQueue = synchronizedList(new ArrayList<>());
    }

    @Override
    public void addMember(Long id, String name) {
        Member member = new Member(id, name);
        readyQueue.stream()
                .filter(m -> Objects.equals(m.getId(), member.getId()))
                .findAny()
                .ifPresentOrElse(m -> {
                    return;
                }, () -> readyQueue.add(member));
    }

    @Override
    public void removeById(Long id) {
        readyQueue.removeIf(m -> Objects.equals(m.getId(), id));
    }

    @Override
    public List<Member> findAll() {
        return readyQueue;
    }

    @Override
    public Member findById(Long id) {
        return readyQueue.stream()
                .filter(member -> Objects.equals(member.getId(), id))
                .findAny()
                .orElse(null);
    }

    @Override
    public Long countMembers() {
        return (long) readyQueue.size();
    }

    @Override
    public Member popFront() {
        if (readyQueue.size() == 0) {
            return null;
        }
        return readyQueue.remove(0);
    }
}
