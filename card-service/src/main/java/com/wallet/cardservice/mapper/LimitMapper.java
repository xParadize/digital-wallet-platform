package com.wallet.cardservice.mapper;

import com.wallet.cardservice.dto.LimitDto;
import com.wallet.cardservice.entity.Limit;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface LimitMapper {
    LimitDto toDto(Limit limit);
}
