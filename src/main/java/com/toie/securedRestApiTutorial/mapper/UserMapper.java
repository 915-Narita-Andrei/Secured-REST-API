package com.toie.securedRestApiTutorial.mapper;

import com.toie.securedRestApiTutorial.domain.RegisterRequestDto;
import com.toie.securedRestApiTutorial.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper
public interface UserMapper {

    @Mapping(target = "password", source = "password")
    User map(RegisterRequestDto registerRequestDto, UUID id, String password);
}
