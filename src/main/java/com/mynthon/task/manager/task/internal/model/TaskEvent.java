package com.mynthon.task.manager.task.internal.model;

import org.springframework.modulith.events.Externalized;

@Externalized("task::assigned")
public record TaskEvent(
        Task task
) {
}
