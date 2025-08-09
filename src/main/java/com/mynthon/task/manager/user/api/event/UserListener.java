package com.mynthon.task.manager.user.api.event;
import com.mynthon.task.manager.bot.TaskManagerTelegramBot;
import com.mynthon.task.manager.reminder.api.dto.response.ReminderResponse;
import com.mynthon.task.manager.reminder.api.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.mynthon.task.manager.bot.utils.StringTelegramBotCommand.REMINDER_READ;
import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.USER_REMINDER_QUEUE;


@Component
@RequiredArgsConstructor
@Slf4j
public class UserListener {

    private final ReminderService service;
    private final TaskManagerTelegramBot bot;

    @RabbitListener(queues = USER_REMINDER_QUEUE)
    public void handleReminderAssignment(@Payload ReminderResponse response) throws TelegramApiException {
        log.info("Получено напоминание из RabbitMQ - {}",response);
        bot.execute(SendMessage.builder()
                        .chatId(response.chatId())
                        .text(createResponseReminder(response.id(),response.taskName(),response.username(),response.time()))
                        .parseMode("HTML")
                .build());
        log.info("Напоминание отправлено, сохранение статуса об успешной отправке - {}",response.id());
        service.setReminderStatus(response.id());
    }

    private String createResponseReminder(Integer id,String taskName,String username, LocalDateTime time){
        String timeStr = time.format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"));
        return String.format("""
                <b>Напоминание для %s под id - {%s}\
                
                Название задачи - {%s}
                Запланированное время - {%s}
                
                Отметить как прочитанное</b> - %s""",username,id,taskName,timeStr, REMINDER_READ + id);
    }
}
