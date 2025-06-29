package com.wallet.authservice.mapper;

import com.wallet.authservice.dto.SignUpRequest;
import com.wallet.authservice.entity.UnverifiedUser;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface UnverifiedUserMapper {
    UnverifiedUser toEntity(SignUpRequest signUpRequest);
}
