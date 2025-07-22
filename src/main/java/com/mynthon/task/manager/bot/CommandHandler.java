package com.mynthon.task.manager.bot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static com.mynthon.task.manager.bot.utils.StringTelegramBotCommand.*;

@Component
@RequiredArgsConstructor
public class CommandHandler {

    public SendMessage handlerMessage(String message,String username,Long chatId){
        switch (message){
            case START ->  {
                return startMessage(username,chatId);
            }
            case REGISTERED -> {
                return createNewUser(username,chatId);
            }
            case ADD_TASK -> {
                return createNewTask(chatId);
            }
            default -> {
                return SendMessage.builder().chatId(chatId).text(username).text("Неизвестная команда").build();
            }
        }
    }

    private SendMessage startMessage(String username,Long chatId){
            String startMessage = String.format("Привествую тебя %s в моем task manager bot, " +
                    "здесь можно будет ставить себе задачи и настривать напоминаии о них",username);
            return SendMessage.builder()
                    .chatId(chatId)
                    .text(startMessage)
                    .build();
    }

    private SendMessage createNewUser(String username, Long chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(String.format("📝 *Форма регистрации*\n\n" +
                        "Имя: %s\n" +
                        "email: `не указан`\n\n" +
                        "password: 'не указан'" +
                        "Выберите поле для редактирования:", username))
                .parseMode("MarkDownV2")
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboardRow(List.of(
                                InlineKeyboardButton.builder()
                                        .text("✏️ Email")
                                        .callbackData("edit_email_user")
                                        .build(),
                                InlineKeyboardButton.builder()
                                        .text("✏️ password")
                                        .callbackData("edit_password_user")
                                        .build()))
                        .keyboardRow(List.of(
                                InlineKeyboardButton.builder()
                                        .text("✅ Готово")
                                        .callbackData("submit_user")
                                        .build()))
                        .build())
                .build();
    }

    private SendMessage createNewTask(Long chatId){
        return SendMessage.builder()
                .chatId(chatId)
                .text("""
                        📝 *Форма создания задачи*
                        
                        Название: `не указано`
                        Описание: `не указано`
                        
                        Выберите поле для редактирования:""")
                .parseMode("MarkdownV2")
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboardRow(List.of(InlineKeyboardButton.builder()
                                        .text("✏️ Название")
                                        .callbackData("edit_task_name")
                                        .build(),
                                InlineKeyboardButton.builder()
                                        .text("✏️ Описание")
                                        .callbackData("edit_task_content")
                                        .build()))
                        .keyboardRow(List.of(
                                InlineKeyboardButton.builder()
                                        .text("✅ Готово")
                                        .callbackData("submit_task")
                                        .build()))
                        .build())
                .build();
    }
}
