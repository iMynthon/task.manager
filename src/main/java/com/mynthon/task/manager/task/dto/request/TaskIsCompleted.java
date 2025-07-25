package com.mynthon.task.manager.task.dto.request;

public record TaskIsCompleted(
        Integer id,
        String nickname,
        Boolean isCompleted
) {
}
