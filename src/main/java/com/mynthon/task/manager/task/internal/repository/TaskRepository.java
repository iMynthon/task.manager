package com.mynthon.task.manager.task.internal.repository;

import com.mynthon.task.manager.task.internal.model.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task,Integer> {

    @EntityGraph(attributePaths = {"user"})
    List<Task> findByUserId(Integer userId);

    @Modifying
    @Query(value = "UPDATE tasks SET is_completed = :isCompleted WHERE id = :id AND user_id = :userId",nativeQuery = true)
    void isCompletedTrue(Integer id,Integer userId,boolean isCompleted);

    @Modifying
    @Query(value = "DELETE from tasks WHERE id = :id AND user_id = :userId",nativeQuery = true)
    void deleteTask(Integer id,Integer userId);
}
