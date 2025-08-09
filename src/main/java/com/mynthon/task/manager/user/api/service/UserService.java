package com.mynthon.task.manager.user.api.service;

import com.mynthon.task.manager.common.exception.EntityNotFoundException;
import com.mynthon.task.manager.user.internal.model.User;
import com.mynthon.task.manager.user.internal.repository.UserRepository;
import com.mynthon.task.manager.user.internal.repository.projections.UserIdProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserIdProjection userIdProjection(String username){
        return userRepository.findByUsername(username).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь под таким никнеймом - {%s} не зарегистрирован в система, ошибка запроса",
                        username)));
    }

    @Transactional
    public User existsUser(String username,Long chatId){
        if(!userRepository.existsUserByChatId(chatId)){
            return User.builder().username(username).chatId(chatId).build();
        }
        return userRepository.findByChatId(chatId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с таким - {%s} идентификатором не найден, ошибка запроса",chatId)));
    }
}
