package com.mynthon.task.manager.task.api.service;

import com.mynthon.task.manager.common.exception.EntityNotFoundException;
import com.mynthon.task.manager.task.api.dto.request.TaskDeleteRequest;
import com.mynthon.task.manager.task.api.dto.request.TaskIsCompleted;
import com.mynthon.task.manager.task.api.dto.request.TaskRequest;
import com.mynthon.task.manager.task.api.dto.response.AllTaskResponse;
import com.mynthon.task.manager.task.api.dto.response.TaskResponse;
import com.mynthon.task.manager.common.mapper.TaskMapper;
import com.mynthon.task.manager.task.internal.model.Task;
import com.mynthon.task.manager.task.internal.repository.TaskRepository;
import com.mynthon.task.manager.user.api.service.UserService;
import com.mynthon.task.manager.user.internal.model.User;
import com.mynthon.task.manager.user.internal.repository.projections.UserIdProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final TaskMapper taskMapper;
    private final ApplicationContext context;

    public TaskResponse findByIdToResponse(Integer id){
        log.info("Запрос задачи по id - {}",id);
        Task task = context.getBean(TaskService.class).findById(id);
        return taskMapper.entityToResponse(task);
    }

    @Transactional(readOnly = true)
    public AllTaskResponse findByMeTasks(String username){
        log.info("Поиск задачи по username - {}",username);
        UserIdProjection id = userService.userIdProjection(username);
        return taskMapper.entityListToResponseList(taskRepository.findByUserIdAndIsCompletedFalse(id.getId()));
    }

    @Transactional
    public TaskResponse save(TaskRequest request){
        log.info("Сохранение новой задачи - {}",request);
        Task task = taskMapper.requestToEntity(request);
        task.setIsCompleted(false);
        task.setUser(checkoutUser(request.getUsername(),request.getChatId()));
        return taskMapper.entityToResponse(taskRepository.save(task));
    }

    @Transactional
    public String isCompleted(TaskIsCompleted isCompleted){
        log.info("Задача выолнена - {}",isCompleted);
        taskRepository.isCompletedTrue(isCompleted.id(),isCompleted.isCompleted());
        return String.format("Поздравляю %s  - завершением задачи",isCompleted.username());
    }

    @Transactional
    public String delete(TaskDeleteRequest deleteRequest){
        log.info("Удаление задачи - {}",deleteRequest);
        taskRepository.deleteById(deleteRequest.id());
        return String.format("Задача пользователя %s под id - %s удалена",
                deleteRequest.nickname(),deleteRequest.id());
    }

    private User checkoutUser(String username,Long chatId){
        return userService.existsUser(username,chatId);
    }

    @Transactional(readOnly = true)
    public Task findById(Integer id){
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Task под таким идентификатором - {%s} не найдена",id)));
    }
}
