package com.mynthon.task.manager.user.event;
import com.mynthon.task.manager.common.exception.EntityNotFoundException;
import com.mynthon.task.manager.task.internal.model.Task;
import com.mynthon.task.manager.user.internal.model.User;
import com.mynthon.task.manager.user.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserListener {

    private final UserRepository userRepository;

    @RabbitListener(queues = "#{@taskQueue}")
    public void handleTaskAssignment(@Payload Task task) {
        log.info("Получено задание из RabbitMQ - {}",task);
        User user = userRepository.findByUsername(task.getUser().getUsername())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Пользователь %s не найден", task.getUser().getUsername())));
        task.setUser(user);
        userRepository.save(user);
    }
}
