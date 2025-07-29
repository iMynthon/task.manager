package com.mynthon.task.manager.reminder.internal.model;

import com.mynthon.task.manager.task.internal.model.Task;
import com.mynthon.task.manager.user.internal.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity(name = "reminders")
public class Reminder {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "user_username")
    private User user;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "task_id")
    private Task task;

    private Long chatId;

    private LocalDateTime time;

}
