package com.tymofiivoitenko.telegram.repository;

import com.tymofiivoitenko.telegram.model.MemReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface JpaMemReactionRepository extends JpaRepository<MemReaction, Integer> {
    // По названию метода Spring сам поймет, что мы хотим получить пользователя по переданному chatId
    List<MemReaction> getByMemTestId(int memTestId);
    //MemReaction getById(int memeReactionId);
}