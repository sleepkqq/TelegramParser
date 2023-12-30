package com.sleepkqq.telegramparser.service;

import com.sleepkqq.telegramparser.model.Video;
import com.sleepkqq.telegramparser.repository.AudioRepository;
import com.sleepkqq.telegramparser.repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
@PropertySource("application.properties")
public class VideoEditor {

    @Autowired
    private AudioRepository audioRepository;
    @Autowired
    private VideoRepository videoRepository;
    @Value("${path.videos}")
    private String DIRECTORY_PATH;
    @Value("${path.idea}")
    private String IDEA_DIRECTORY_PATH;

    public void generateVideo(String videoFileName, String srcMeme) {
        log.info("Начало загрузки видео");
        try {
            String audioFileName = getRandomAudioFile();
            String videoInfo = getRandomVideoInfoByFileName(videoFileName);

            ProcessBuilder processBuilder = new ProcessBuilder("python", IDEA_DIRECTORY_PATH + "src\\main\\python\\VideoEditor.py",
                    videoInfo, audioFileName, srcMeme, DIRECTORY_PATH);
            Process process = processBuilder.start();

            InputStream inputStream = process.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Читаем вывод Python-скрипта и выводим его в консоль
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Ждем завершения выполнения скрипта
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Python скрипт выполнен успешно");
            } else {
                log.error("Python скрипт завершился с ошибкой. Код завершения: " + exitCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("Видео успешно скачано");
    }

    private String getRandomAudioFile() {
        try {
            return audioRepository.findById(new Random().nextLong(audioRepository.count()) + 1).get().getFileName();
        } catch (Exception e) {
            return getRandomAudioFile();
        }
    }

    private String getRandomVideoInfoByFileName(String fileName) {
        try {
            List<Video> videos = videoRepository.getVideosByFileName(fileName);
            Video randomVideo = videos.get(new Random().nextInt(videos.size()) + 1);
            return randomVideo.getFileName() + "+" + randomVideo.getBegin();
        } catch (Exception e) {
            return getRandomVideoInfoByFileName(fileName);
        }
    }

}
