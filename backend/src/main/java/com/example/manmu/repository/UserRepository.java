//user의 CRUD를 담당하는 UserRepository
//기본적으로 인터페이스 이다.

package com.example.manmu.repository;

import com.example.manmu.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
