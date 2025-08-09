package com.mynthon.task.manager.user.internal.repository;

import com.mynthon.task.manager.user.internal.model.User;
import com.mynthon.task.manager.user.internal.repository.projections.UserIdProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<UserIdProjection> findByUsername(String username);

    Optional<User> findByChatId(Long chatId);

    boolean existsUserByChatId(Long chatId);
}
