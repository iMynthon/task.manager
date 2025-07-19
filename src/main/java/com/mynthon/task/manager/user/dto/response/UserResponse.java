package com.mynthon.task.manager.user.dto.response;

public record UserResponse(
        Integer id,
        String nickname,
        String email
) {
}
