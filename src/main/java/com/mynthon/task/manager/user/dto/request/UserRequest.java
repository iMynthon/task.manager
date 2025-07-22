package com.mynthon.task.manager.user.dto.request;

public record UserRequest(
        String nickname,
        String email,
        String password
){
}
