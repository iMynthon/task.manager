package com.mynthon.task.manager.task.api.dto.request;

public record TaskDeleteRequest(
        Integer id,
        String nickname
) {
}
