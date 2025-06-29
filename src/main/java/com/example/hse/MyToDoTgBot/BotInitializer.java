package com.example.hse.MyToDoTgBot;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.xml.bind.SchemaOutputResolver;

@Configuration
public class BotInitializer {
    private final ToDoBot toDoBot;

    public BotInitializer(ToDoBot toDoBot) {
        this.toDoBot = toDoBot;
    }

    @PostConstruct
    public void init() throws TelegramApiException{
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(toDoBot);
        System.out.println("Бот успешно зарегистрирован и готов к работе!");
    }
}
