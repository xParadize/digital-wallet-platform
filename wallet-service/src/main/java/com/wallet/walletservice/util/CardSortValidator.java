package com.wallet.walletservice.util;

import com.wallet.walletservice.dto.CardSort;
import com.wallet.walletservice.enums.CardSortOrder;
import com.wallet.walletservice.enums.CardSortType;
import com.wallet.walletservice.exception.CardSortException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CardSortValidator {
    private final Map<CardSortType, CardSortOrder> defaultSortTypeOrder = Map.of(
            CardSortType.RECENT, CardSortOrder.ASC,
            CardSortType.NAME, CardSortOrder.AALPH,
            CardSortType.BALANCE, CardSortOrder.DESC,
            CardSortType.EXPIRATION, CardSortOrder.EARLIEST,
            CardSortType.LIMIT, CardSortOrder.DESC
    );

    private final Map<CardSortType, List<CardSortOrder>> availableSortTypeOrders = Map.of(
            CardSortType.RECENT, List.of(CardSortOrder.ASC),
            CardSortType.NAME, List.of(CardSortOrder.AALPH, CardSortOrder.DALPH),
            CardSortType.BALANCE, List.of(CardSortOrder.ASC, CardSortOrder.DESC),
            CardSortType.EXPIRATION, List.of(CardSortOrder.EARLIEST, CardSortOrder.LATEST),
            CardSortType.LIMIT, List.of(CardSortOrder.ASC, CardSortOrder.DESC)
    );

    public CardSort validateSort(String sort, String order) {
        CardSortType defaultSortType = CardSortType.BALANCE;
        CardSortOrder defaultSortOrder = CardSortOrder.DESC;

        if (sort == null && order == null) {
            return new CardSort(defaultSortType, defaultSortOrder);
        }

        CardSortType sortType;
        CardSortOrder sortOrder;

        if (sort == null) {
            sortType = defaultSortType;
        } else {
            try {
                sortType = CardSortType.valueOf(sort.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CardSortException("Invalid sort type");
            }
        }

        if (order == null) {
            sortOrder = defaultSortTypeOrder.get(sortType);
        } else {
            try {
                sortOrder = CardSortOrder.valueOf(order.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new CardSortException("Invalid sort order");
            }

            List<CardSortOrder> availableOrders = availableSortTypeOrders.get(sortType);
            if (!availableOrders.contains(sortOrder)) {
                throw new CardSortException("This sorting algorithm doesn't support the specified order");
            }
        }

        return new CardSort(sortType, sortOrder);
    }
}