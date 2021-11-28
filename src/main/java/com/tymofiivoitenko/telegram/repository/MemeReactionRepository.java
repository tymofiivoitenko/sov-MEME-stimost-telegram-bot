package com.tymofiivoitenko.telegram.repository;

import com.tymofiivoitenko.telegram.model.meme.MemeReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface MemeReactionRepository extends JpaRepository<MemeReaction, Integer> {
    List<MemeReaction> getByMemeTestId(int memTestId);

    @Query(value = "select * from meme_reaction where DATE(meme_reaction.created_at) = DATE(now())", nativeQuery = true)
    List<MemeReaction> getNewReactions();
}