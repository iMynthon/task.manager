package com.mynthon.task.manager.reminder.api.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReminderResponse(
        Integer id,
        String username,
        Long chatId,
        String taskName,
        LocalDateTime time
) {
}
