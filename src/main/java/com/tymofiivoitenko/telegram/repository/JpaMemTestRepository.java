package com.tymofiivoitenko.telegram.repository;

import com.tymofiivoitenko.telegram.model.MemTest;
import com.tymofiivoitenko.telegram.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface JpaMemTestRepository extends JpaRepository<MemTest, Integer> {
    // По названию метода Spring сам поймет, что мы хотим получить пользователя по переданному chatId
    Optional<MemTest> getByCreateByUser(int userId);
}