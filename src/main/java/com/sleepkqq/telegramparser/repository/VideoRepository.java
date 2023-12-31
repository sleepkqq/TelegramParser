package com.sleepkqq.telegramparser.repository;

import com.sleepkqq.telegramparser.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> getVideosByFileName(String fileName);
}
