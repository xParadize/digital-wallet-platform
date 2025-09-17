package com.wallet.walletservice.dto;

import com.wallet.walletservice.enums.CardSortOrder;
import com.wallet.walletservice.enums.CardSortType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CardSort {
    CardSortType type;
    CardSortOrder order;
}
