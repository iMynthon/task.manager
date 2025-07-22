package com.mynthon.task.manager.task.internal.mapper;

import com.mynthon.task.manager.task.dto.request.TaskRequest;
import com.mynthon.task.manager.task.dto.response.AllTaskResponse;
import com.mynthon.task.manager.task.dto.response.TaskResponse;
import com.mynthon.task.manager.task.internal.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {

    Task requestToEntity(TaskRequest request);

    TaskResponse entityToResponse(Task task);

    default AllTaskResponse entityListToResponseList(List<Task> tasks){
        return new AllTaskResponse(tasks.stream()
                .map(this::entityToResponse).toList());
    }
}
