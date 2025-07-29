package com.mynthon.task.manager.bot.handler;

import com.mynthon.task.manager.bot.utils.StringTelegramBotCommand;
import com.mynthon.task.manager.common.configuration.RabbitMQConfig;
import com.mynthon.task.manager.reminder.dto.request.ReminderRequest;
import com.mynthon.task.manager.task.dto.request.TaskRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mynthon.task.manager.bot.utils.StringTelegramBotCommand.*;
import static com.mynthon.task.manager.bot.utils.StringUtils.*;
import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.REMINDER_EVENTS_EXCHANGE;
import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.REMINDER_QUEUE_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderHandler {

    public static final Map<Long, String> stateUserReminderEdit = new HashMap<>();
    private static final Map<Long, ReminderRequest> createReminderRequest = new HashMap<>();
    private final RabbitTemplate rabbitTemplate;

    public SendMessage inlineKeyboardNewReminder(Long chatId) {
        log.info("Создание напоминания для задачи - {}", chatId);
        return SendMessage.builder()
                .chatId(chatId)
                .text("Форма создания напоминания\nВыберите поле для редактирования:")
                .parseMode("MarkdownV2")
                .replyMarkup(keyboardMarkupReminderTask(""))
                .build();
    }

    public SendMessage handlerReminderEditor(Long chatId,String reminderRequest){
        switch (reminderRequest) {
            case EDIT_TASK_NAME -> {
                log.info("Изменение названия задачи - {} - {}", chatId, reminderRequest);
                stateUserReminderEdit.put(chatId, reminderRequest);
                return new SendMessage(chatId.toString(), "Введите название задачи:");
            }
            case TIME_REMINDER -> {
                log.info("Изменение описания задачи - {} - {}", chatId, reminderRequest);
                stateUserReminderEdit.put(chatId, reminderRequest);
                return new SendMessage(chatId.toString(), "Введите время напоминания:");
            }
            default -> {
                return new SendMessage(chatId.toString(), "Неизвестное действие");
            }
        }
    }

    public SendMessage createReminder(Long chatId,String username,String message,String state){
        SendMessage sendMessage = new SendMessage();
        ReminderRequest request = createReminderRequest.getOrDefault(chatId,ReminderRequest.builder()
                .build());
        if (state.equals(EDIT_TASK_NAME)) {
            request.setTaskName(message);
            sendMessage = createSendMessageFromReminder(chatId,request,username,message);
        } else if (state.equals(TIME_REMINDER)) {
            request.setTime(LocalDateTime.parse(message));
            sendMessage = createSendMessageFromReminder(chatId,request,username,message);
        }
        if(request.isComplete()){
            rabbitTemplate.convertAndSend(REMINDER_EVENTS_EXCHANGE,REMINDER_QUEUE_KEY,request);
            createReminderRequest.remove(chatId);
            stateUserReminderEdit.remove(chatId);
            sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
            sendMessage.setChatId(chatId);
            sendMessage.setText(REMINDER_CREATE);
            return sendMessage;
        }
        return sendMessage;
    }

    private SendMessage createSendMessageFromReminder(Long chatId,ReminderRequest request,
                                                  String username,String operationTask){
        request.setUsername(username);
        request.setChatId(chatId);
        stateUserReminderEdit.remove(chatId);
        createReminderRequest.put(chatId, request);
        log.info("Сохранение описания задачи - {} - {}", username, request);
        return SendMessage.builder()
                .chatId(chatId)
                .text(operationTask.equals(EDIT_TASK_NAME) ? TASK_NAME_CREATE : TASK_CONTENT_CREATE)
                .replyMarkup(request.isComplete() ? null :
                        keyboardMarkupReminderTask(operationTask.equals(EDIT_TASK_NAME) ? EDIT_TASK_NAME : EDIT_TASK_CONTENT))
                .build();
    }

    private ReplyKeyboardMarkup keyboardMarkupReminderTask(String reminder){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow key1 = new KeyboardRow();
        if(reminder.equals(EDIT_TASK_NAME)){
            key1.add(TIME_REMINDER);
        } else if (reminder.equals(TIME_REMINDER)){
            key1.add(EDIT_TASK_NAME);
        } else {
            key1.add(EDIT_TASK_NAME);
            key1.add(TIME_REMINDER);
        }
        keyboardMarkup.setKeyboard(List.of(key1));
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setSelective(true);
        return keyboardMarkup;
    }
}
