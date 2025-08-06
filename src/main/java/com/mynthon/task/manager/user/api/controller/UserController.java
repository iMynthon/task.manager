package com.mynthon.task.manager.user.api.controller;

import com.mynthon.task.manager.user.api.dto.request.UserRequest;
import com.mynthon.task.manager.user.api.dto.response.AllUserResponse;
import com.mynthon.task.manager.user.api.dto.response.UserResponse;
import com.mynthon.task.manager.user.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public UserResponse findByUserId(@PathVariable Integer id){
        return userService.findById(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/all")
    public AllUserResponse findAllUsers(){
        return userService.findAll();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    public UserResponse save(@RequestBody UserRequest request){
        return userService.save(request);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}/delete")
    public String deleteById(@PathVariable Integer id){
        return userService.delete(id);
    }
}
