package com.mynthon.task.manager.task.dto.request;

public record TaskIsCompleted(
        String nickname,
        String name,
        Boolean isCompleted
) {
}
