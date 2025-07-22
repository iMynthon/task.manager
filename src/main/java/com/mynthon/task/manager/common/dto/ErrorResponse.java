package com.mynthon.task.manager.common.dto;

public record ErrorResponse(
        int code,
        String message
) {
}
