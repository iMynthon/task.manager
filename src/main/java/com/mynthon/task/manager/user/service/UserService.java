package com.mynthon.task.manager.user.service;

import com.mynthon.task.manager.exception.EntityNotFoundException;
import com.mynthon.task.manager.user.dto.request.UserRequest;
import com.mynthon.task.manager.user.dto.response.AllUserResponse;
import com.mynthon.task.manager.user.dto.response.UserResponse;
import com.mynthon.task.manager.user.mapper.UserMapper;
import com.mynthon.task.manager.user.model.User;
import com.mynthon.task.manager.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse findById(Integer id){
        log.info("Поиск пользователя по id - {}",id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User под идентификатором - {%s} не зарегистрирован",id)));
        return userMapper.entityToResponse(user);
    }

    @Transactional(readOnly = true)
    public AllUserResponse findAll(){
        log.info("Поиск всех пользолвателей");
        return userMapper.listEntityToListResponse(userRepository.findAll());
    }

    public UserResponse save(UserRequest request){
        log.info("Сохранение нового пользователя - {}",request);
        User user = userMapper.requestToEntity(request);
        return userMapper.entityToResponse(userRepository.save(user));
    }

    public String delete(Integer id){
        log.info("Удаление пользователя под id - {}",id);
        userRepository.deleteById(id);
        return String.format("User под идентификатором - %s успешно удален",id);
    }

}
