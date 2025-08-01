package com.mynthon.task.manager.reminder.events;

import com.mynthon.task.manager.common.configuration.RabbitMQConfig;
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

import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.*;
import static com.mynthon.task.manager.reminder.internal.model.ReminderStatus.WAITING;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderListener {

    private final ReminderService service;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = REMINDER_QUEUE)
    public void handleReminderAssigned(@Payload ReminderRequest request){
        log.info("Сохранение напоминание пользователя - {}",request.getUsername());
        long delayMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), request.getTime());
        if(delayMillis < 0){
            log.info("Время напоминание просрочено, некорректное время - {}",request.getTime());
            return;
        }
        request.setStatus(WAITING);
        ReminderResponse response = service.save(request);
        log.info("Новое напоминание - {}",response);
        log.info("Задержка отправки: {} мс ({} минут)", delayMillis, delayMillis / 60000.0);
        rabbitTemplate.convertAndSend(REMINDER_EVENTS_EXCHANGE, USER_REMINDER_RT_KEY,response, msg -> {
           msg.getMessageProperties().setDelayLong(delayMillis);
           return msg;
        });
    }
}
