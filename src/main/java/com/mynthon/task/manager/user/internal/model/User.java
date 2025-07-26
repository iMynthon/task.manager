package com.mynthon.task.manager.user.internal.model;

import com.mynthon.task.manager.reminder.internal.model.Reminder;
import com.mynthon.task.manager.task.internal.model.Task;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    private String email;

    private Long chatId;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<Task> taskList;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<Reminder> reminderList;
}
