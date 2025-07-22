package com.mynthon.task.manager.task.controller;

import com.mynthon.task.manager.task.dto.request.TaskDeleteRequest;
import com.mynthon.task.manager.task.dto.request.TaskIsCompleted;
import com.mynthon.task.manager.task.dto.request.TaskRequest;
import com.mynthon.task.manager.task.dto.response.AllTaskResponse;
import com.mynthon.task.manager.task.dto.response.TaskResponse;
import com.mynthon.task.manager.task.internal.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}/me")
    private TaskResponse findById(@PathVariable Integer id){
        return taskService.findById(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/all/me")
    private AllTaskResponse findByMeAll(@RequestParam String nickname){
        return taskService.findByMeTasks(nickname);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    private TaskResponse save(@RequestBody TaskRequest request){
        return taskService.save(request);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/isCompleted")
    private String updateIsCompleted(TaskIsCompleted isCompleted){
        return taskService.isCompleted(isCompleted);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete")
    private String deleteMeTask(TaskDeleteRequest deleteRequest){
        return taskService.delete(deleteRequest);
    }

}
