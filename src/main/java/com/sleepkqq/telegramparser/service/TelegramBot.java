package com.sleepkqq.telegramparser.service;

import com.sleepkqq.telegramparser.config.BotConfig;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@PropertySource("application.properties")
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    @Autowired
    private VideoEditor videoEditor;
    @Autowired
    private MemesParser memesParser;
    @Value("${channel.name}")
    private String CHANNEL_NAME;
    private static List<String> srcMemes;
    private static int memeNumber;

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

    @Scheduled(cron = "0 0 * * * *")
    private void uploadMemeTelegram() {
        try {
            if (srcMemes == null) {
                log.info("srcMemes is null");
                return;
            }

            String srcMeme = srcMemes.get(memeNumber);
            memeNumber++;

            SendPhoto sendPhoto = new SendPhoto(CHANNEL_NAME, new InputFile(srcMeme));
            sendPhoto.setCaption("@memes_every_day");

            execute(sendPhoto);
            log.info("Опубликован новый пост в Телеграм");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 10 4 * * *")
    private void updateMemesDB() {
        srcMemes = memesParser.downloadNewMemes();
        memeNumber = 0;

        log.info("Успешно обновлен список srcMemes");

        for (int i = 0; i < 10; i++) {
            System.out.println("-".repeat(200));
            videoEditor.generateVideo("10min.mp4" , srcMemes.get(i));
            System.out.println("-".repeat(200));
        }
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
