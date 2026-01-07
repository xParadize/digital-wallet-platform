package com.wallet.transactionservice.mapper;

import com.wallet.transactionservice.dto.TransactionDto;
import com.wallet.transactionservice.dto.TransactionEvent;
import com.wallet.transactionservice.dto.TransactionInfoDto;
import com.wallet.transactionservice.entity.Transaction;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {
    @Mapping(source = "offer.vendor", target = "vendor")
    @Mapping(source = "offer.category", target = "category")
    @Mapping(source = "confirmedAt", target = "completedAt")
    TransactionDto toDto(Transaction transaction);

    @Mapping(source = "offer.id", target = "offerId", qualifiedByName = "extractOfferId")
    TransactionEvent toEvent(Transaction transaction);

    @Mapping(source = "offer.vendor", target = "vendor")
    @Mapping(source = "offer.category", target = "category")
    @Mapping(source = "confirmedAt", target = "completedAt")
    @Mapping(source = "fee", target = "commission")
    TransactionInfoDto toInfo(Transaction transaction);

    @Named("extractOfferId")
    default Long extractOfferId(String offerId) {
        if (offerId == null) {
            return null;
        }
        String numericPart = offerId.replaceFirst("^pmt-", "");
        return Long.parseLong(numericPart);
    }
}