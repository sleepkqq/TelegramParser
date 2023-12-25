package com.sleepkqq.telegramparser.service;

import com.sleepkqq.telegramparser.repository.AudioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

@Component
@Slf4j
@PropertySource("application.properties")
public class VideoOperations {

    @Autowired
    private AudioRepository audioRepository;
    @Value("${video.path}")
    private String DIRECTORY_PATH;
    @Value("${video.path.idea}")
    private String IDEA_DIRECTORY_PATH;
    @Value("${video.path.completed}")
    private String COMPLETED_DIRECTORY_PATH;

    public void downloadVideo(String link) {
        log.info("Начало загрузки видео");
        try {
            URL url = new URL(link);
            URLConnection connection = url.openConnection();

            String fileName = createFile(link);
            String filePath = DIRECTORY_PATH + fileName;

            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(filePath)) {

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            String audioFileName = getRandomAudioFile();

            ProcessBuilder processBuilder = new ProcessBuilder("python", IDEA_DIRECTORY_PATH + "src\\main\\python\\VideoEditor.py", fileName, audioFileName);
            Process process = processBuilder.start();

            // Ждем завершения выполнения скрипта
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Python скрипт выполнен успешно");
            } else {
                log.error("Python скрипт завершился с ошибкой. Код завершения: " + exitCode);
            }

            deleteFile(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("Видео успешно скачано");
    }

    public String createFile(String link) {
        String[] params = link.split("/");
        String fileName = params[params.length - 1];

        Path path = Paths.get(fileName);

        try {
            Files.createFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileName;
    }

    public void deleteFile(String filePath) {
        File videoFile = new File(filePath);
        File emptyVideoFile = new File(IDEA_DIRECTORY_PATH + videoFile.getName());
        videoFile.delete();
        emptyVideoFile.delete();
    }

    private String getRandomAudioFile() {
        try {
            return audioRepository.findById(new Random().nextLong(audioRepository.count()) + 1).get().getFileName();
        } catch (Exception e) {
            return getRandomAudioFile();
        }
    }

}
