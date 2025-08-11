package com.mynthon.task.manager.reminder.api.events;

import com.mynthon.task.manager.reminder.api.dto.request.ReminderRequest;
import com.mynthon.task.manager.reminder.api.dto.response.ReminderResponse;

import com.mynthon.task.manager.reminder.api.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.*;
import static com.mynthon.task.manager.reminder.internal.model.ReminderStatus.WAITING;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class ReminderListener {

    private final ReminderService service;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = REMINDER_QUEUE)
    public void handleReminderAssigned(@Payload ReminderRequest request){
        try {
            log.info("Сохранение напоминание пользователя - {}", request.getUsername());
            if (request.getTime().isBefore(LocalDateTime.now())) {
                log.info("Время напоминание просрочено, некорректное время - {}", request.getTime());
                return;
            }
            request.setStatus(WAITING);
            log.info("Сохранение напоминания");
            service.save(request);
        }catch (AmqpException ae){
            log.info("Ошибка при прослушивании сообщение - {}",ae.getMessage());
        }
    }

    @Scheduled(fixedRate = 45000)
    public void reminderMessage(){
        List<ReminderResponse> list = service.checkWaitingReminder();
        if(!list.isEmpty()){
            log.info("Напоминания - {}",list.size());
            for(ReminderResponse rm : list){
                log.info("Напоминания для пользователя - {}",rm.username());
                try {
                    rabbitTemplate.convertAndSend(REMINDER_EVENTS_EXCHANGE, USER_REMINDER_RT_KEY, rm);
                }catch (AmqpException ae){
                    log.info("Ошибка отправки сообщения - {}",ae.getMessage());
                }
            }
        }
    }
}
