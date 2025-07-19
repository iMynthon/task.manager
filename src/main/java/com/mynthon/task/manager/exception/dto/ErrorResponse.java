package com.mynthon.task.manager.exception.dto;

public record ErrorResponse(
        int code,
        String message
) {
}
