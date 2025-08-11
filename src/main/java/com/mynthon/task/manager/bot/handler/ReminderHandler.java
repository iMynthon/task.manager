package com.mynthon.task.manager.bot.handler;

import com.mynthon.task.manager.common.feign.ReminderFeignClient;
import com.mynthon.task.manager.reminder.api.dto.request.ReminderRequest;
import com.mynthon.task.manager.reminder.api.dto.response.AllReminderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.mynthon.task.manager.bot.utils.StringTelegramBotCommand.*;
import static com.mynthon.task.manager.bot.utils.StringUtils.*;
import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.MAIN_EVENTS_TOPIC;
import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.REMINDER_RT_KEY;
import static com.mynthon.task.manager.reminder.internal.model.ReminderStatus.PENDING;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderHandler {

    public static final Map<Long, String> stateUserReminderEdit = new HashMap<>();
    private static final Map<Long, ReminderRequest> createReminderRequest = new HashMap<>();
    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
    private final RabbitTemplate rabbitTemplate;
    private final ReminderFeignClient reminderFeignClient;

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
            case REMINDER_TASK_ID -> {
                log.info("Изменение названия задачи - {} - {}", chatId, reminderRequest);
                stateUserReminderEdit.put(chatId, reminderRequest);
                return new SendMessage(chatId.toString(), "Введите идентификатор {id} задачи:");
            }
            case REMINDER_TIME -> {
                log.info("Изменение описания задачи - {} - {}", chatId, reminderRequest);
                stateUserReminderEdit.put(chatId, reminderRequest);
                return new SendMessage(chatId.toString(), "Введите время напоминания в формате -> " +
                        "{Год.Месяц.День} {Часы:Минуты}  {2025.12.30} {18:00}:");
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
        if (state.equals(REMINDER_TASK_ID)) {
            request.setTaskId(Integer.parseInt(message));
            sendMessage = createSendMessageFromReminder(chatId,request,username, REMINDER_TASK_ID);
        } else if (state.equals(REMINDER_TIME)) {
            request.setTime(LocalDateTime.parse(message, format));
            if(request.getTime().isBefore(LocalDateTime.now())){
                sendMessage.setChatId(chatId);
                sendMessage.setText(TIME_OVERDUE + ": " + request.getTime());
                return sendMessage;
            }
            sendMessage = createSendMessageFromReminder(chatId,request,username,
                    REMINDER_TIME + ": " + request.getTime());
        }
        if(request.isComplete()){
            log.info("Отправка напоминания в слушатель событий - {}",request.getUsername());
            request.setStatus(PENDING);
            rabbitTemplate.convertAndSend(MAIN_EVENTS_TOPIC, REMINDER_RT_KEY,request);
            createReminderRequest.remove(chatId);
            stateUserReminderEdit.remove(chatId);
            sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
            sendMessage.setChatId(chatId);
            sendMessage.setText(REMINDER_CREATE + ": " + request.getTime().format(format));
            return sendMessage;
        }
        return sendMessage;
    }

    public SendMessage getReminders(Long chatId,String username){
        log.info("Запрос на просмотр всех упоминаний от - {}",username);
        AllReminderResponse allReminderResponse = reminderFeignClient.getAll(username);
        if(allReminderResponse.reminderList().isEmpty()){
            return SendMessage.builder()
                    .chatId(chatId)
                    .text("Список напоминаний пуст")
                    .build();
        }
        String userReminder = String.format("<b>Список напоминаний пользователя: %s</b>\n\n",username);
        String message = userReminder + allReminderResponse.reminderList()
                .stream().map(reminder -> createMessage(reminder.taskName(),reminder.time().format(format),reminder.id()))
                .collect(Collectors.joining("\n\n"));
        return SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .parseMode("HTML")
                .build();
    }

    public SendMessage readAndDeleteReminder(Long chatId, String message){
        log.info("Запрос прочитать уведомление от пользователя - {}",message);
        String messageResponse = "";
        if(message.contains(REMINDER_READ)) {
            Integer id = Integer.parseInt(message.substring(REMINDER_READ.length()));
            messageResponse = reminderFeignClient.accepted(id);
        } else if (message.contains(REMINDER_DELETE)){
            Integer id = Integer.parseInt(message.substring(REMINDER_DELETE.length()));
            messageResponse = reminderFeignClient.delete(id);
        }
        return SendMessage.builder()
                .chatId(chatId)
                .text(messageResponse)
                .build();
    }

    private SendMessage createSendMessageFromReminder(Long chatId,ReminderRequest request,
                                                  String username,String operationTask){
        request.setUsername(username);
        request.setChatId(chatId);
        stateUserReminderEdit.remove(chatId);
        createReminderRequest.put(chatId, request);
        log.info("Сохранение напоминания - {} - {}", username, request.getTaskId());
        return SendMessage.builder()
                .chatId(chatId)
                .text(operationTask.equals(REMINDER_TASK_ID) ? REMINDER_SAVE_TASK_NAME : REMINDER_SAVE_TASK_TIME)
                .replyMarkup(request.isComplete() ? null :
                        keyboardMarkupReminderTask(operationTask.equals(REMINDER_TASK_ID) ? REMINDER_TASK_ID
                                : REMINDER_TIME))
                .build();
    }

    private ReplyKeyboardMarkup keyboardMarkupReminderTask(String reminder){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow key1 = new KeyboardRow();
        if(reminder.equals(REMINDER_TASK_ID)){
            key1.add(REMINDER_TIME);
        } else if (reminder.equals(REMINDER_TIME)){
            key1.add(REMINDER_TASK_ID);
        } else {
            key1.add(REMINDER_TASK_ID);
            key1.add(REMINDER_TIME);
        }
        keyboardMarkup.setKeyboard(List.of(key1));
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setSelective(true);
        return keyboardMarkup;
    }

    private String createMessage(String taskName,String timeStr,Integer id){
        return String.format("""
                <b>Название задачи: {%s}
                Запланированное время: {%s}
                Удалить напоминание: %s</b>
                """,taskName,timeStr,REMINDER_DELETE + id);
    }
}
