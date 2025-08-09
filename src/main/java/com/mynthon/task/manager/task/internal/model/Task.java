package com.mynthon.task.manager.task.internal.model;
import com.mynthon.task.manager.reminder.internal.model.Reminder;
import com.mynthon.task.manager.user.internal.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String content;

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    private User user;

    @CreationTimestamp
    @Column(name = "create_at")
    private LocalDate createAt;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    @OneToMany(mappedBy = "task",cascade = CascadeType.ALL)
    private List<Reminder> taskReminder;
}
