package com.mynthon.task.manager.task.api.dto.request;

public record TaskIsCompleted(
        Integer id,
        String nickname,
        Boolean isCompleted
) {
}
