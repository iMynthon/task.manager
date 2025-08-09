package com.mynthon.task.manager.reminder.internal.repository;

import com.mynthon.task.manager.reminder.internal.model.Reminder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder,Integer> {

    @EntityGraph(attributePaths = {"user"})
    List<Reminder> findByUserId(Integer userId);

    @Query(value = "SELECT * from reminders where status = :waiting or status = :sent AND time <= :time", nativeQuery = true)
    List<Reminder> findByStatusAndTime(@Param("waiting") String waiting,String sent,@Param("time") LocalDateTime time);

    @Modifying
    @Query(value = "UPDATE reminders SET status = :status WHERE id = :id", nativeQuery = true)
    void setStatusReminder(@Param("status") String status, @Param("id") Integer id);
}
