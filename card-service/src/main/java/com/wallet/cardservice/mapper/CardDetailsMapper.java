package com.wallet.cardservice.mapper;

import com.wallet.cardservice.dto.CardDetailsDto;
import com.wallet.cardservice.entity.CardDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardDetailsMapper {
    CardDetailsDto toDto(CardDetails cardDetails);
}
