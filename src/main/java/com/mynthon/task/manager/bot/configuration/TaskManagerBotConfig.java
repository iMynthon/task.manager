package com.mynthon.task.manager.bot.configuration;

import com.mynthon.task.manager.bot.TaskManagerTelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TaskManagerBotConfig {
    @Bean
    public TelegramBotsApi telegramBotsApi(TaskManagerTelegramBot taskManagerTelegramBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(taskManagerTelegramBot);
        return api;

    }
}
