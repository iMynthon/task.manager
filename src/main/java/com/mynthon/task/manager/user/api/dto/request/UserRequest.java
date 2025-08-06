package com.mynthon.task.manager.user.api.dto.request;

public record UserRequest(
        String nickname,
        String email,
        String password
){
}
