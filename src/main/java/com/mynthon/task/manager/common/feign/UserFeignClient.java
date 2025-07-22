package com.mynthon.task.manager.common.feign;

import com.mynthon.task.manager.user.dto.request.UserRequest;
import com.mynthon.task.manager.user.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@FeignClient(name = "user")
public interface UserFeignClient {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    UserResponse save(@RequestBody UserRequest request);
}
