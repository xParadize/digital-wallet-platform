package com.wallet.transactionservice.mapper;

import com.wallet.transactionservice.dto.PaymentOffer;
import com.wallet.transactionservice.entity.PaymentOfferEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentOfferMapper {

    @Mapping(source = "amount.value", target = "amount")
    @Mapping(source = "amount.currency", target = "currency")
    @Mapping(source = "location.vendor", target = "vendor")
    @Mapping(source = "location.latitude", target = "latitude")
    @Mapping(source = "location.longitude", target = "longitude")
    PaymentOfferEntity toEntity(PaymentOffer paymentOffer);
}
