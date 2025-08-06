package com.mynthon.task.manager.user.api.dto.response;

import java.util.List;

public record AllUserResponse(
        List<UserResponse> listUsers
) {
}
