package com.tymofiivoitenko.telegram.repository;

import com.tymofiivoitenko.telegram.model.MemeTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface JpaMemeTestRepository extends JpaRepository<MemeTest, Integer> {
    // Get meme test created by userId
    Optional<MemeTest> getByCreateByUser(int userId);
}