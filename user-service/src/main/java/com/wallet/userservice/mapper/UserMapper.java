package com.wallet.userservice.mapper;

import com.wallet.userservice.entity.UnverifiedUser;
import com.wallet.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toEntity(UnverifiedUser unverifiedUser);
}
