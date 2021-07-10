package com.tymofiivoitenko.telegram.repository;

import com.tymofiivoitenko.telegram.model.MemeReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface JpaMemeReactionRepository extends JpaRepository<MemeReaction, Integer> {
    // По названию метода Spring сам поймет, что мы хотим получить пользователя по переданному chatId
    List<MemeReaction> getByMemeTestId(int memTestId);
    //MemReaction getById(int memeReactionId);
}