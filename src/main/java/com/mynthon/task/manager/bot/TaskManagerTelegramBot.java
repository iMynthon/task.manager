package com.mynthon.task.manager.bot;
import com.mynthon.task.manager.bot.handler.CommandHandler;
import com.mynthon.task.manager.common.exception.EmptyInputException;
import com.mynthon.task.manager.common.exception.SendMessageBotException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Objects;

@Component
public class TaskManagerTelegramBot extends TelegramLongPollingBot{

    @Value("${telegram.bot.username}")
    private String username;

    private CommandHandler commandHandler;

    public TaskManagerTelegramBot(@Value("${telegram.bot.token}") String token,CommandHandler commandHandler) {
        super(token);
        this.commandHandler = commandHandler;
    }

    @Override
    public String getBotUsername() {
        return this.username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            String username = update.getMessage().getChat().getUserName();
            Long chatId = update.getMessage().getChatId();
            SendMessage sendMessage = commandHandler.handlerMessage(message,username, chatId);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new SendMessageBotException(String.format("Ошибка при отправке ссобщения -> %s",sendMessage.toString()));
            }
        } else if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();
            SendMessage sendMessage = commandHandler.handleCallback(chatId,callbackData);
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new SendMessageBotException(String.format("Ошибка при отправке ссобщения -> %s",sendMessage.toString()));
            }
        } else {
            throw new EmptyInputException("Ошибка при обработке сообщения -> пустой ввод");
        }
    }
}
