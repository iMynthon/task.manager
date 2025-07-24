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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public TaskResponse findById(Integer id){
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Task под таким идентификатором - {%s} не найдена",id)));
        return taskMapper.entityToResponse(task);
    }

    @Transactional(readOnly = true)
    public AllTaskResponse findByMeTasks(String nickname){
        return taskMapper.entityListToResponseList(taskRepository.findByNicknameIgnoreCase(nickname));
    }

    public TaskResponse save(TaskRequest request){
        Task task = taskMapper.requestToEntity(request);
        task.setIsCompleted(false);
        return taskMapper.entityToResponse(taskRepository.save(task));
    }

    @Transactional
    public String isCompleted(TaskIsCompleted isCompleted){
        taskRepository.isCompletedTrue(isCompleted.nickname(),isCompleted.name(),isCompleted.isCompleted());
        return String.format("Поздравляю %s  - завершением задачи",isCompleted.nickname());
    }

    @Transactional
    public String delete(TaskDeleteRequest deleteRequest){
        taskRepository.deleteTask(deleteRequest.nickname(),deleteRequest.name());
        return String.format("Задача пользователя под никнеймом %s под названием - %s удалена",
                deleteRequest.nickname(),deleteRequest.name());
    }
}
