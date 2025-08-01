package com.mynthon.task.manager.task.internal.service;

import com.mynthon.task.manager.common.exception.EntityNotFoundException;
import com.mynthon.task.manager.task.dto.request.TaskDeleteRequest;
import com.mynthon.task.manager.task.dto.request.TaskIsCompleted;
import com.mynthon.task.manager.task.dto.request.TaskRequest;
import com.mynthon.task.manager.task.dto.response.AllTaskResponse;
import com.mynthon.task.manager.task.dto.response.TaskResponse;
import com.mynthon.task.manager.task.internal.mapper.TaskMapper;
import com.mynthon.task.manager.task.internal.model.Task;
import com.mynthon.task.manager.task.internal.repository.TaskRepository;
import com.mynthon.task.manager.user.internal.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mynthon.task.manager.common.configuration.RabbitMQConfig.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationContext context;

    public TaskResponse findByIdToResponse(Integer id){
        log.info("Запрос задачи по id - {}",id);
        Task task = context.getBean(TaskService.class).findById(id);
        return taskMapper.entityToResponse(task);
    }

    @Transactional(readOnly = true)
    public AllTaskResponse findByMeTasks(String nickname){
        log.info("Поиск задачи по nickname - {}",nickname);
        return taskMapper.entityListToResponseList(taskRepository.findByUserUsernameIgnoreCase(nickname));
    }

    public TaskResponse save(TaskRequest request){
        log.info("Сохранение новой задачи - {}",request);
        Task task = taskMapper.requestToEntity(request);
        task.setIsCompleted(false);
        task.setUser(createUser(request.getUsername(),request.getChatId()));
        log.info("Отправка сообщения с помощью RabbitMQ");
        rabbitTemplate.convertAndSend(MAIN_EVENTS_TOPIC,TASK_RT_KEY,task);
        return taskMapper.entityToResponse(taskRepository.save(task));
    }

    @Transactional
    public String isCompleted(TaskIsCompleted isCompleted){
        log.info("Задача выолнена - {}",isCompleted);
        taskRepository.isCompletedTrue(isCompleted.id(),isCompleted.nickname(),isCompleted.isCompleted());
        return String.format("Поздравляю %s  - завершением задачи",isCompleted.nickname());
    }

    @Transactional
    public String delete(TaskDeleteRequest deleteRequest){
        log.info("Удаление задачи - {}",deleteRequest);
        taskRepository.deleteTask(deleteRequest.nickname(),deleteRequest.name());
        return String.format("Задача пользователя под никнеймом %s под названием - %s удалена",
                deleteRequest.nickname(),deleteRequest.name());
    }

    private User createUser(String username,Long chatId){
        return User.builder().username(username).chatId(chatId).build();
    }

    @Transactional(readOnly = true)
    public Task findById(Integer id){
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Task под таким идентификатором - {%s} не найдена",id)));
    }
}
