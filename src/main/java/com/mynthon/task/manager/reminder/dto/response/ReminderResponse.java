package com.mynthon.task.manager.reminder.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReminderResponse(
        String username,
        Long chatId,
        String taskName,
        LocalDateTime time
) {
}
