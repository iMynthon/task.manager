package com.mynthon.task.manager.task.internal.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nickname;

    private String name;

    private String content;

    @CreationTimestamp
    @Column(name = "create_at")
    private LocalDate createAt;

    @Column(name = "is_completed")
    private Boolean isCompleted;
}
