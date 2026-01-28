package com.wallet.cardservice.mapper;

import com.wallet.cardservice.dto.Holder;
import com.wallet.cardservice.dto.HolderDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface HolderMapper {
    Holder toEntity(HolderDto holderDto);
    HolderDto toDto(Holder holder);
}
