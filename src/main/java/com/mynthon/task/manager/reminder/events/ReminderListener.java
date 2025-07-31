package com.mynthon.task.manager.reminder.events;

import com.mynthon.task.manager.reminder.dto.request.ReminderRequest;
import com.mynthon.task.manager.reminder.dto.response.ReminderResponse;
import com.mynthon.task.manager.reminder.internal.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.REMINDER_QUEUE_KEY;
import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.USER_REMINDER_TASK;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderListener {

    private final ReminderService service;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = REMINDER_QUEUE_KEY)
    public void handleReminderAssigned(@Payload ReminderRequest request){
        log.info("Сохранение напоминание пользователя - {}",request.getUsername());
        long delayMinutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), request.getTime());
        if(delayMinutes < 0){
            log.info("Время напоминание просрочено, некорректное время - {}",request.getTime());
            return;
        }
        ReminderResponse response = service.save(request);
        log.info("Новое напоминание - {}",response);
        rabbitTemplate.convertAndSend(USER_REMINDER_TASK,response, msg -> {
           msg.getMessageProperties().setDelayLong(delayMinutes);
           return msg;
        });
    }
}
