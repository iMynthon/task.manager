package com.mynthon.task.manager.user.event;
import com.mynthon.task.manager.bot.TaskManagerTelegramBot;
import com.mynthon.task.manager.common.exception.EntityNotFoundException;
import com.mynthon.task.manager.reminder.dto.response.ReminderResponse;
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

import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.TASK_QUEUE_KEY;
import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.USER_REMINDER_TASK;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserListener {

    private final UserRepository userRepository;
    private final TaskManagerTelegramBot bot;

    @RabbitListener(queues = TASK_QUEUE_KEY)
    public void handleTaskAssignment(@Payload Task task) {
        log.info("Получено задание из RabbitMQ - {}",task);
        User user = userRepository.findByUsername(task.getUser().getUsername())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не найден", task.getUser().getUsername())));
        task.setUser(user);
        userRepository.save(user);
    }

    @RabbitListener(queues = USER_REMINDER_TASK)
    public void handleReminderAssignment(@Payload ReminderResponse response) throws TelegramApiException {
        log.info("Получено напоминание из RabbitMQ - {}",response);
        bot.execute(SendMessage.builder()
                        .chatId(response.chatId())
                        .text(response.taskName() + " -> " + response.time())
                .build());
    }
}
