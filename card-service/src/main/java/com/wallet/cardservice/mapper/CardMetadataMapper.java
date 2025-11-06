package com.wallet.cardservice.mapper;

import com.wallet.cardservice.dto.CardMetadataDto;
import com.wallet.cardservice.entity.CardMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMetadataMapper {
    CardMetadataDto toDto(CardMetadata cardMetadata);
}
