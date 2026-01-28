package com.wallet.transactionservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FeeService {
    @Value("${transaction.commission.transfer-threshold}")
    private BigDecimal LARGE_TRANSFER_THRESHOLD;

    @Value("${transaction.commission.fee.cap}")
    private BigDecimal MAX_FEE_CAP;

    @Value("${transaction.commission.fee.rate}")
    private BigDecimal APP_FEE_RATE;

    public BigDecimal applyTransferFee(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount can't be null");
        }

        if (amount.compareTo(LARGE_TRANSFER_THRESHOLD) < 0) {
            return amount;
        }

        BigDecimal commission = amount.multiply(APP_FEE_RATE);
        commission = commission.min(MAX_FEE_CAP);

        return amount.add(commission);
    }
}
