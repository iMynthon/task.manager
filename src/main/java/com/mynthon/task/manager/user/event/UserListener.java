package com.mynthon.task.manager.user.event;
import com.mynthon.task.manager.common.configuration.EventConfig;
import com.mynthon.task.manager.common.exception.EntityNotFoundException;
import com.mynthon.task.manager.task.internal.model.Task;
import com.mynthon.task.manager.task.internal.model.TaskEvent;
import com.mynthon.task.manager.user.internal.model.User;
import com.mynthon.task.manager.user.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.TASK_QUEUE_KEY;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserListener {

    private final UserRepository userRepository;

    @RabbitListener(queues = TASK_QUEUE_KEY)
    public void handleTaskAssignment(TaskEvent taskEvent) {
        Task task = taskEvent.task();
        log.info("Получено задание из RabbitMQ");
        User user = userRepository.findByUsername(task.getUser().getUsername())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не найден", task.getUser().getUsername())));
        task.setUser(user);
        userRepository.save(user);
    }
}
