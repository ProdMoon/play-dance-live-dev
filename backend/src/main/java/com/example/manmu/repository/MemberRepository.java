package com.example.manmu.repository;

import com.example.manmu.domain.Member;

import java.util.List;

public interface MemberRepository {
    void addMember(Long id, String name);

    void removeById(Long id);

    List<Member> findAll();

    Member findById(Long id);

    Long countMembers();
    Member popFront();
}
