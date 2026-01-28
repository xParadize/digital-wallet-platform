package com.wallet.cardservice.mapper;

import com.wallet.cardservice.dto.CardDto;
import com.wallet.cardservice.dto.SaveCardDto;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.CardDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface CardMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "balance", source = "money")
    @Mapping(target = "cardDetails", expression = "java(mapToCardDetails(saveCardDto))")
    @Mapping(target = "cardMetadata", ignore = true)
    @Mapping(target = "limit", ignore = true)
    Card toEntity(SaveCardDto saveCardDto);

    default CardDetails mapToCardDetails(SaveCardDto dto) {
        return CardDetails.builder()
                .number(dto.getNumber())
                .expirationDate(dto.getExpirationDate())
                .cvv(dto.getCvv())
                .build();
    }

    CardDto toDto(Card card);
}
