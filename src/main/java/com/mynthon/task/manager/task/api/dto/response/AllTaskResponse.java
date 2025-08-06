package com.mynthon.task.manager.task.api.dto.response;

import java.util.List;

public record AllTaskResponse(
        List<TaskResponse> listTasks
) {
}
