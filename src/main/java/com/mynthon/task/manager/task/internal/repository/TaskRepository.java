package com.mynthon.task.manager.task.internal.repository;

import com.mynthon.task.manager.task.internal.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task,Integer> {

    List<Task> findByNicknameIgnoreCase(String nickname);

    @Modifying
    @Query(value = "UPDATE tasks SET is_completed = :isCompleted WHERE id = :id AND nickname = :nickname",nativeQuery = true)
    void isCompletedTrue(Integer id,String nickname,boolean isCompleted);

    @Modifying
    @Query(value = "DELETE from tasks WHERE nickname = :nickname AND name = :name",nativeQuery = true)
    void deleteTask(String nickname,String name);
}
