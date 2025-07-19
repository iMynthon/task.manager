package com.mynthon.task.manager.user.mapper;

import com.mynthon.task.manager.user.dto.request.UserRequest;
import com.mynthon.task.manager.user.dto.response.AllUserResponse;
import com.mynthon.task.manager.user.dto.response.UserResponse;
import com.mynthon.task.manager.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    User requestToEntity(UserRequest request);

    UserResponse entityToResponse(User user);

    default AllUserResponse listEntityToListResponse(List<User> users){
        return new AllUserResponse(users.stream().map(this::entityToResponse)
                .toList());
    }
}
