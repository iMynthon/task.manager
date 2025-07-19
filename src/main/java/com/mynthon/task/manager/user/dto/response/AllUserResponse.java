package com.mynthon.task.manager.user.dto.response;

import java.util.List;

public record AllUserResponse(
        List<UserResponse> listUsers
) {
}
