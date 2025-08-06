package com.mynthon.task.manager.task.api.dto.response;

import java.time.LocalDate;

public record TaskResponse(
        Integer id,
        String name,
        String content,
        LocalDate createAt,
        Boolean isCompleted
) {
}
