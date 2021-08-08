package com.tymofiivoitenko.telegram.repository;

import com.tymofiivoitenko.telegram.model.meme.MemeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface JpaMemeImageRepository extends JpaRepository<MemeImage, Integer> {
    @Query(nativeQuery = true, value = "SELECT id FROM meme_image where active = true ORDER BY random() LIMIT 13")
    List<Integer> getRandomMemes();
}