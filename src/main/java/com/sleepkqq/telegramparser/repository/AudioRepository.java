package com.sleepkqq.telegramparser.repository;

import com.sleepkqq.telegramparser.model.Audio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AudioRepository extends JpaRepository<Audio, Long> {
}
