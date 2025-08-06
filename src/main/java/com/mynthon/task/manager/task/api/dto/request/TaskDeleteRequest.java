package com.mynthon.task.manager.task.api.dto.request;

public record TaskDeleteRequest(
        String nickname,
        String name
) {
}
