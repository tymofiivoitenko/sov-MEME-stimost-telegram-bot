package com.tymofiivoitenko.telegram.repository;

import com.tymofiivoitenko.telegram.model.MemImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public interface JpaMemImageRepository extends JpaRepository<MemImage, Integer> {
    @Query(nativeQuery = true, value = "SELECT id FROM mem_image ORDER BY random() LIMIT 13")
    List<Integer> getRandomMems();
}