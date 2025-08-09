package com.mynthon.task.manager.common.feign;

import com.mynthon.task.manager.reminder.api.dto.response.AllReminderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@FeignClient("reminders")
public interface ReminderFeignClient {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    AllReminderResponse getAll(@RequestParam String username);

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}/accepted")
    String accepted(@PathVariable Integer id);
}
