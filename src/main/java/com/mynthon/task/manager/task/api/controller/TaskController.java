package com.mynthon.task.manager.task.api.controller;

import com.mynthon.task.manager.task.api.dto.request.TaskDeleteRequest;
import com.mynthon.task.manager.task.api.dto.request.TaskIsCompleted;
import com.mynthon.task.manager.task.api.dto.request.TaskRequest;
import com.mynthon.task.manager.task.api.dto.response.AllTaskResponse;
import com.mynthon.task.manager.task.api.dto.response.TaskResponse;
import com.mynthon.task.manager.task.api.service.TaskService;
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
        return taskService.findByIdToResponse(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/all/me")
    private AllTaskResponse findByMeAll(@RequestParam String username){
        return taskService.findByMeTasks(username);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    private TaskResponse save(@RequestBody TaskRequest request){
        return taskService.save(request);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/isCompleted")
    private String updateIsCompleted(@RequestBody TaskIsCompleted isCompleted){
        return taskService.isCompleted(isCompleted);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/delete")
    private String deleteMeTask(@RequestBody TaskDeleteRequest deleteRequest){
        return taskService.delete(deleteRequest);
    }

}
