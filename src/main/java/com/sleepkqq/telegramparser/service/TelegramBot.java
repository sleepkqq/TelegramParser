package com.sleepkqq.telegramparser.service;

import com.sleepkqq.telegramparser.config.BotConfig;
import com.sleepkqq.telegramparser.service.upload.YoutubeUpload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@PropertySource("application.properties")
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    @Autowired
    private VideoCreatorAPI videoCreatorAPI;
    @Autowired
    private VideoOperations videoOperations;
    @Autowired
    private YoutubeUpload youTubeUpload;
    @Autowired
    private MemesParser memesParser;
    @Value("${channel.name}")
    private String CHANNEL_NAME;
    private static List<String> srcMemes;

    public TelegramBot(BotConfig config) {
        this.config = config;

        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/start", "get a welcome message"));
        try {
            this.execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println(update.getMessage().getText());
    }

    @Scheduled(initialDelay = 5 * 1000, fixedRate = 3 * 60 * 1000)
    private void uploadMeme() {
        System.out.println("-".repeat(200));
        log.info("Начало процесса создания и публикации контента");
        try {
            if (srcMemes == null) {
                log.info("srcMemes is null");
                return;
            }

            String srcMeme = srcMemes.get(0);
            srcMemes.remove(0);

            double imageRatio = getImageAspectRatio(srcMeme);
            imageRatio = imageRatio > 0.75 ? imageRatio - (imageRatio - 0.75) * 2 : imageRatio;


            String link = videoCreatorAPI.createVideo(srcMeme, imageRatio);
            videoOperations.downloadVideo(link);

            SendPhoto sendPhoto = new SendPhoto(CHANNEL_NAME, new InputFile(srcMeme));
            sendPhoto.setCaption("t.me/test_channel_sduhfsiudhf");

            execute(sendPhoto);
            log.info("Опубликован новый пост в Телеграм");

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("-".repeat(200));
    }

    private static double getImageAspectRatio(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        BufferedImage image = ImageIO.read(url);

        int width = image.getWidth();
        int height = image.getHeight();

        return (double) width / height;
    }

    @Scheduled(fixedRate = 18 * 60 * 1000)
    private void updateMemesDB() {
        srcMemes = memesParser.downloadNewMemes();

        System.out.println("-".repeat(200));
        log.info("Успешно обновлен список srcMemes");
        System.out.println("-".repeat(200));
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}
