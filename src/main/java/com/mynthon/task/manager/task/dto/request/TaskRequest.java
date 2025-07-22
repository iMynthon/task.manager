package com.mynthon.task.manager.task.dto.request;

public record TaskRequest(
        String name,
        String content,
        String nickname
) {
}
