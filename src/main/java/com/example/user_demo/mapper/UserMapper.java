package com.example.user_demo.mapper;

import com.example.user_demo.dto.request.RegisterRequest;
import com.example.user_demo.dto.request.UpdateUserRequest;
import com.example.user_demo.dto.response.UserResponse;
import com.example.user_demo.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    User toEntity(RegisterRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateEntity(UpdateUserRequest request, @MappingTarget User user);
}
