package com.mynthon.task.manager.reminder.internal.repository;

import com.mynthon.task.manager.reminder.internal.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReminderRepository extends JpaRepository<Reminder,Integer> {

    List<Reminder> findByUserUsernameAndTaskId(String username, Integer id);
}
