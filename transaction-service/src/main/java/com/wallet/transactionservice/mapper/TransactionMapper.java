package com.wallet.transactionservice.mapper;

import com.wallet.transactionservice.dto.TransactionDto;
import com.wallet.transactionservice.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "cardNumber", ignore = true)
    @Mapping(source = "confirmedAt", target = "completedAt")
    TransactionDto toDto(Transaction transaction);
}
