package com.mynthon.task.manager.bot;
import com.mynthon.task.manager.bot.handler.CommandHandler;
import com.mynthon.task.manager.common.exception.EmptyInputException;
import com.mynthon.task.manager.common.exception.SendMessageBotException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static com.mynthon.task.manager.bot.utils.StringUtils.USERNAME_EMPTY;

@Component
@Slf4j
public class TaskManagerTelegramBot extends TelegramLongPollingBot{

    @Value("${telegram.bot.username}")
    private String username;

    private final CommandHandler commandHandler;

    public TaskManagerTelegramBot(@Value("${telegram.bot.token}") String token,CommandHandler commandHandler) {
        super(token);
        this.commandHandler = commandHandler;
    }

    @Override
    public String getBotUsername() {
        return this.username;
    }

    @Override
    public void onRegister() {
        try {
            log.info("Отправка команд бота");
            execute(new SetMyCommands(createBotMenuCommands(), new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            throw new SendMessageBotException(String.format("Ошибка при отправке доступных команд -> %s",e.getMessage()));
        }

    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            try{
            String message = update.getMessage().getText();
            String username = update.getMessage().getChat().getUserName();
            Long chatId = update.getMessage().getChatId();
            if(username.isBlank()){
                log.info("Пользователь не определен, не задано имя пользователя в настройках тг - {}",chatId);
                execute(SendMessage.builder()
                        .chatId(chatId)
                        .text(USERNAME_EMPTY)
                        .build());
            }
            log.info("Ввод команды пользователем - {}",username);
            SendMessage sendMessage = commandHandler.handlerMessage(message,username, chatId);
            execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new SendMessageBotException(String.format("Ошибка при отправке ссобщения -> %s",e.getMessage()));
            }
        } else {
            throw new EmptyInputException("Ошибка при обработке сообщения -> пустой ввод");
        }
    }

    private List<BotCommand> createBotMenuCommands(){
        return List.of(new BotCommand("/start","Начало работы бота"),
                new BotCommand("/help","Список доступных команд"),
                new BotCommand("/add_task","Добавление задачи"),
                new BotCommand("/tasks","Просмотр списка задач"),
                new BotCommand("/reminders","Просмотр списка напоминаний"),
                new BotCommand("/reminder_task","Поставить напоминание"));
    }
}
