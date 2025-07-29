package com.mynthon.task.manager.reminder.dto.events;

import com.mynthon.task.manager.reminder.dto.request.ReminderRequest;
import com.mynthon.task.manager.reminder.dto.response.ReminderResponse;
import com.mynthon.task.manager.reminder.internal.model.Reminder;
import com.mynthon.task.manager.reminder.internal.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.REMINDER_QUEUE_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderListener {

    private final ReminderService service;
//    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = REMINDER_QUEUE_KEY)
    public void handleReminderAssigned(@Payload ReminderRequest request){
        log.info("Сохранение напоминание пользователя - {}",request.getUsername());
        ReminderResponse response = service.save(request);
        log.info("Новое напоминание - {}",response);
    }
}
