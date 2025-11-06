package com.wallet.cardservice.service;

import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.CardDetails;
import com.wallet.cardservice.exception.CardDetailsNotFoundException;
import com.wallet.cardservice.mapper.CardDetailsMapper;
import com.wallet.cardservice.repository.CardDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardDetailsService {
    private final CardDetailsRepository cardDetailsRepository;

    @Transactional(readOnly = true)
    public CardDetails getDetailsByCard(Card card) {
        return cardDetailsRepository.findByCard(card)
                .orElseThrow(CardDetailsNotFoundException::new);
    }
}