package com.mynthon.task.manager.task.dto.response;

import java.util.List;

public record AllTaskResponse(
        List<TaskResponse> listTasks
) {
}
