package com.mynthon.task.manager.user.api.dto.response;

public record UserResponse(
        Integer id,
        String nickname,
        String email
) {
}
