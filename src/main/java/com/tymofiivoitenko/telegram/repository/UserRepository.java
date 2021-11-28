package com.tymofiivoitenko.telegram.repository;

import com.tymofiivoitenko.telegram.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> getByChatId(int chatId);

    Optional<User> findByUserName(String userName);

    @Query(value = "select * from users where DATE(users.created_at) = DATE(now());", nativeQuery = true)
    List<User> findNewUsers();
}