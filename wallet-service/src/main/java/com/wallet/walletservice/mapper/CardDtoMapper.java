package com.wallet.walletservice.mapper;

import com.wallet.walletservice.dto.AddCardDto;
import com.wallet.walletservice.dto.SaveCardDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardDtoMapper {
    SaveCardDto toDto(AddCardDto addCardDto);
}

