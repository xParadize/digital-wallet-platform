package com.wallet.cardservice.mapper;

import com.wallet.cardservice.dto.LimitDto;
import com.wallet.cardservice.entity.Limit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardLimitMapper {
    LimitDto toDto(Limit limit);
}
