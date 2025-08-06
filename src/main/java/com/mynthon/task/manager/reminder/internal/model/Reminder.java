package com.mynthon.task.manager.reminder.internal.model;

import com.mynthon.task.manager.task.internal.model.Task;
import com.mynthon.task.manager.user.internal.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.modulith.Modulithic;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity(name = "reminders")
@Modulithic
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "user_username",referencedColumnName = "username")
    private User user;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "task_id")
    private Task task;

    private Long chatId;

    private LocalDateTime time;

    @Enumerated(EnumType.STRING)
    private ReminderStatus status;

}
