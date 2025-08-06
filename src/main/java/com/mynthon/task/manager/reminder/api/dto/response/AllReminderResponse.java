package com.mynthon.task.manager.reminder.api.dto.response;

import java.util.List;

public record AllReminderResponse(
        List<ReminderResponse> reminderList
) {
}
