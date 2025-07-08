package com.wallet.cardservice.mapper;

import com.wallet.cardservice.dto.SaveCardDto;
import com.wallet.cardservice.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {
    @Mapping(target = "id", ignore = true)
    Card toEntity(SaveCardDto saveCardDto);
}
