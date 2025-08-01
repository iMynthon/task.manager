package com.mynthon.task.manager.user.event;
import com.mynthon.task.manager.bot.TaskManagerTelegramBot;
import com.mynthon.task.manager.common.exception.EntityNotFoundException;
import com.mynthon.task.manager.reminder.dto.response.ReminderResponse;
import com.mynthon.task.manager.reminder.internal.service.ReminderService;
import com.mynthon.task.manager.task.internal.model.Task;
import com.mynthon.task.manager.user.internal.model.User;
import com.mynthon.task.manager.user.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.TASK_QUEUE;
import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.USER_REMINDER_QUEUE;


@Component
@RequiredArgsConstructor
@Slf4j
public class UserListener {

    private final UserRepository userRepository;
    private final ReminderService service;
    private final TaskManagerTelegramBot bot;

    @RabbitListener(queues = TASK_QUEUE)
    public void handleTaskAssignment(@Payload Task task) {
        log.info("Получено задание из RabbitMQ - {}",task);
        User user = userRepository.findByUsername(task.getUser().getUsername())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не найден", task.getUser().getUsername())));
        task.setUser(user);
        userRepository.save(user);
    }

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
        return String.format("<b>Напоминание для %s под id - {%s}\nНазвание задачи - {%s}\nЗапланированное время - {%s}</b>",username,id,taskName,timeStr);
    }
}
