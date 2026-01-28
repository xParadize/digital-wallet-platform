package com.wallet.cardservice.service;

import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.CardMetadata;
import com.wallet.cardservice.exception.CardMetadataNotFoundException;
import com.wallet.cardservice.mapper.CardMetadataMapper;
import com.wallet.cardservice.repository.CardMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardMetadataService {
    private final CardMetadataRepository cardMetadataRepository;
    private final CardMetadataMapper cardMetadataMapper;

    @Transactional(readOnly = true)
    public CardMetadata getMetadataByCard(Card card) {
        return cardMetadataRepository.findByCard(card)
                .orElseThrow(CardMetadataNotFoundException::new);
    }
}