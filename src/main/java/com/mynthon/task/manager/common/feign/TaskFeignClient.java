package com.mynthon.task.manager.common.feign;

import com.mynthon.task.manager.task.api.dto.request.TaskIsCompleted;
import com.mynthon.task.manager.task.api.dto.request.TaskRequest;
import com.mynthon.task.manager.task.api.dto.response.AllTaskResponse;
import com.mynthon.task.manager.task.api.dto.response.TaskResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "task")
public interface TaskFeignClient {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    TaskResponse save(@RequestBody TaskRequest request);

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/all/me")
    AllTaskResponse findByMeAll(@RequestParam String nickname);

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/isCompleted")
    String updateIsCompleted(@RequestBody TaskIsCompleted isCompleted);
}
