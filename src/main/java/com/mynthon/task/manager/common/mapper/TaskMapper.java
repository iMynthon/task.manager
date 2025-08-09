package com.mynthon.task.manager.common.mapper;

import com.mynthon.task.manager.task.api.dto.request.TaskRequest;
import com.mynthon.task.manager.task.api.dto.response.AllTaskResponse;
import com.mynthon.task.manager.task.api.dto.response.TaskResponse;
import com.mynthon.task.manager.task.internal.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.modulith.Modulithic;

import java.util.List;

@Modulithic
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TaskMapper {

    @Mapping(target = "user",ignore = true)
    Task requestToEntity(TaskRequest request);

    TaskResponse entityToResponse(Task task);

    default AllTaskResponse entityListToResponseList(List<Task> tasks){
        return new AllTaskResponse(tasks.stream()
                .map(this::entityToResponse).toList());
    }
}
