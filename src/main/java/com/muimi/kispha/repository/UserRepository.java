package com.muimi.kispha.repository;

import com.muimi.kispha.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUid(Long uid);

    User findByUsername(String username);

    User findByEmail(String email);

    List<User> findByUsernameContaining(String username);
}
