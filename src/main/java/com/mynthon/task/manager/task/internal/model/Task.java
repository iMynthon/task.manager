package com.mynthon.task.manager.task.internal.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mynthon.task.manager.reminder.internal.model.Reminder;
import com.mynthon.task.manager.user.internal.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @ManyToOne(cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "user_username")
    private User user;

    @OneToMany(mappedBy = "task",cascade = CascadeType.ALL)
    private List<Reminder> taskReminder;

    private String name;

    private String content;

    @CreationTimestamp
    @Column(name = "create_at")
    private LocalDate createAt;

    @Column(name = "is_completed")
    private Boolean isCompleted;
}
