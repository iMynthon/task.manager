package com.mynthon.task.manager.reminder.api.controller;

import com.mynthon.task.manager.reminder.api.dto.response.AllReminderResponse;
import com.mynthon.task.manager.reminder.api.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public AllReminderResponse getAll(@RequestParam String username){
        return reminderService.getAllReminder(username);
    }
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}/accepted")
    public String accepted(@PathVariable Integer id){
        return reminderService.acceptedReminder(id);
    }
}
