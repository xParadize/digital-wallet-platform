package com.wallet.cardservice.service;

import com.wallet.cardservice.dto.*;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.CardDetails;
import com.wallet.cardservice.entity.CardMetadata;
import com.wallet.cardservice.entity.Limit;
import com.wallet.cardservice.enums.CardSortOrder;
import com.wallet.cardservice.enums.CardSortType;
import com.wallet.cardservice.enums.CardStatus;
import com.wallet.cardservice.event.CardLinkedEvent;
import com.wallet.cardservice.exception.CardAccessDeniedException;
import com.wallet.cardservice.exception.CardNotFoundException;
import com.wallet.cardservice.exception.CardStatusActionException;
import com.wallet.cardservice.feign.TransactionFeignClient;
import com.wallet.cardservice.feign.UserFeignClient;
import com.wallet.cardservice.kafka.CardKafkaProducer;
import com.wallet.cardservice.mapper.*;
import com.wallet.cardservice.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final CardKafkaProducer cardKafkaProducer;
    private final UserFeignClient userFeignClient;
    private final HolderMapper holderMapper;
    private final TransactionFeignClient transactionFeignClient;
    private final CardMetadataService cardMetadataService;
    private final CardDetailsService cardDetailsService;
    private final CardLimitService cardLimitService;
    private final CardMetadataMapper cardMetadataMapper;
    private final CardDetailsMapper cardDetailsMapper;
    private final CardCacheService cardCacheService;
    private final CardLimitMapper cardLimitMapper;

    private final int RECENT_TRANSACTIONS_COUNT = 3;

    @Transactional(readOnly = true)
    public List<CardPreviewDto> getCards(UUID userId, CardSortType sort, CardSortOrder order, int offset, int limit) {
        List<Card> cards = switch (sort) {
            case RECENT -> findAllCardsByLastUse(userId, offset, limit);
            case NAME -> findAllCardsByIssuerName(userId, order, offset, limit);
            case BALANCE -> null;
            case EXPIRATION -> null;
            case LIMIT -> null;
        };

        return cards.stream()
                .map(card -> getCardInfo(userId, card))
                .map(cardInfoDto -> mapToCardPreviewDto(
                        cardInfoDto.getCardDto(),
                        cardInfoDto.getSecretDetails(),
                        cardInfoDto.getCardMetadataDto()
                ))
                .toList();
    }

    private List<Card> findAllCardsByLastUse(UUID userId, int offset, int limit) {
        List<String> lastUsedCardNumbers = transactionFeignClient.getLastUsedCardNumbers(userId, offset, limit);
        if (lastUsedCardNumbers.isEmpty()) {
            return Collections.emptyList();
        }
        return lastUsedCardNumbers.stream()
                .map(cardRepository::findByCardDetails_Number)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private List<Card> findAllCardsByIssuerName(UUID userId, CardSortOrder order, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        switch (order) {
            case DALPH -> {
                return cardRepository.findAllByUserIdOrderByCardMetadata_IssuerDesc(userId, pageable);
            } case AALPH -> {
                return cardRepository.findAllByUserIdOrderByCardMetadata_IssuerAsc(userId, pageable);
            } default -> {
                return List.of();
            }
        }
    }

    private CardPreviewDto mapToCardPreviewDto(CardDto cardDto, CardDetailsDto detailsDto, CardMetadataDto metadataDto) {
        return CardPreviewDto.builder()
                .number(detailsDto.number())
                .issuer(metadataDto.issuer())
                .scheme(metadataDto.paymentScheme())
                .balance(cardDto.balance())
                .build();
    }


//    private List<Card> findAllCardsByBalance(UUID userId, CardSortOrder order) {
//        switch (order) {
//            case DESC -> {
//                return cardRepository.findByUserIdOrderByMoneyDesc(userId);
//            } case ASC -> {
//                return cardRepository.findByUserIdOrderByMoneyAsc(userId);
//            } default -> {
//                return List.of();
//            }
//        }
//    }
//
//
//    private List<Card> findAllCardsByExpiration(UUID userId, CardSortOrder order) {
//        switch (order) {
//            case EARLIEST -> {
//                return cardRepository.findByUserIdOrderByExpirationDateEarliest(userId);
//            } case LATEST -> {
//                return cardRepository.findByUserIdOrderByExpirationDateLatest(userId);
//            } default -> {
//                return List.of();
//            }
//        }
//    }

//
//    private List<Card> findAllCardsByLimit(UUID userId, CardSortOrder order) {
//        switch (order) {
//            case DESC -> {
//                return cardRepository.findByUserIdOrderByLimitValueDesc(userId);
//            } case ASC -> {
//                return cardRepository.findByUserIdOrderByLimitValueAsc(userId);
//            } default -> {
//                return List.of();
//            }
//        }
//    }

    @Transactional(readOnly = true)
    public CardInfoDto getCardById(Long cardId, UUID userId) {
        CardInfoDto cached = getCardInfoFromCache(cardId, userId);
        if (cached != null) {
            return cached;
        }
        return loadAndCacheCardInfo(cardId, userId);
    }

    private CardInfoDto getCardInfoFromCache(Long cardId, UUID userId) {
        CardInfoDto cachedCardInfo = cardCacheService.getCardById(cardId, userId);
        if (cachedCardInfo == null) {
            return null;
        }
        List<TransactionDto> recentTransactions = getRecentTransactions(cachedCardInfo.getSecretDetails().number());
        cachedCardInfo.setRecentTransactions(recentTransactions);
        return cachedCardInfo;
    }

    private CardInfoDto loadAndCacheCardInfo(Long cardId, UUID userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (!isCardLinkedToUser(card.getId(), userId)) {
            throw new CardAccessDeniedException("Access to the card is forbidden");
        }

        CardInfoDto cardInfoDto = getCardInfo(userId, card);

        cardCacheService.saveCard(cardId, userId, cardInfoDto);

        cardInfoDto.setRecentTransactions(getRecentTransactions(cardInfoDto.getSecretDetails().number()));
        return cardInfoDto;
    }

    private CardInfoDto getCardInfo(UUID userId, Card card) {
        CardDetails cardDetails = cardDetailsService.getDetailsByCard(card);
        CardMetadata cardMetadata = cardMetadataService.getMetadataByCard(card);
        Limit limit = cardLimitService.getLimitByCard(card);

        return CardInfoDto.builder()
                .cardDto(cardMapper.toDto(card))
                .cardMetadataDto(cardMetadataMapper.toDto(cardMetadata))
                .holder(getCardHolder(userId))
                .secretDetails(cardDetailsMapper.toDto(cardDetails))
                .recentTransactions(Collections.emptyList())
                .limit(cardLimitMapper.toDto(limit))
                .build();
    }

    private Holder getCardHolder(UUID userId) {
        HolderDto dto = userFeignClient.getCardHolder(userId).getBody();
        return holderMapper.toEntity(dto);
    }

    private List<TransactionDto> getRecentTransactions(String cardNumber) {
        return transactionFeignClient.getRecentTransactions(cardNumber, RECENT_TRANSACTIONS_COUNT);
    }

//    @Transactional
//    public void saveCard(SaveCardDto saveCardDto) {
//        Card card = cardMapper.toEntity(saveCardDto);
//        enrichCardWithMeta(card);
//
//        Limit defaultLimit = Limit.builder()
//                .perTransactionLimit(new BigDecimal("1000000"))
//                .limitEnabled(true)
//                .build();
//
//        Limit savedLimit = limitRepository.save(defaultLimit);
//        card.setLimit(savedLimit);
//
//        Card savedCard = cardRepository.save(card);
//        savedLimit.setCard(savedCard);
//
//        sendCardLinkedEvent(saveCardDto.getUserId(), saveCardDto.getEmail(), maskCardNumber(card.getNumber()), card.getCardIssuer(), card.getCardScheme(), Instant.now());
//    }
//
//    private void enrichCardWithMeta(Card card) {
//        CardMeta meta = cardInfoCollector.getCardMeta(card.getNumber());
//        card.setCardIssuer(meta.issuer());
//        card.setCardScheme(meta.scheme());
//    }

    private void sendCardLinkedEvent(UUID userId, String email, String maskedNumber, String issuer, String scheme, Instant linkedAt) {
        CardLinkedEvent event = new CardLinkedEvent(userId, email, maskedNumber, issuer, scheme, linkedAt);
        cardKafkaProducer.sendCardLinkedEvent(event);
    }

    private String maskCardNumber(String number) {
        return "*" + number.substring(number.length() - 4);
    }

    public boolean isCardLinkedToUser(Long cardId, UUID userId) {
        return cardRepository.findById(cardId)
                .map(card -> card.getUserId().equals(userId))
                .orElse(false);
    }

//    public Card getCardByNumber(String number) {
//        return cardRepository.getCardByNumber(number).orElseThrow(() -> new CardNotFoundException("Card not found"));
//    }

//    @Transactional
//    public void freeze(String number, String email, UUID userId) {
//        Card card = getCardByNumber(number);
//        card.setFrozen(true);
//        cardRepository.save(card);
//        cardKafkaProducer.sendCardFrozenEvent(maskCardNumber(number), email, userId);
//    }
//
//    @Transactional
//    public void unfreeze(String number, String email, UUID userId) {
//        Card card = getCardByNumber(number);
//        card.setFrozen(false);
//        cardRepository.save(card);
//        cardKafkaProducer.sendCardUnfrozenEvent(maskCardNumber(number), email, userId);
//    }
//
//    @Transactional
//    public void block(String number, String email, UUID userId) {
//        Card card = getCardByNumber(number);
//        card.setBlocked(true);
//        cardRepository.save(card);
//        cardKafkaProducer.sendCardBlockedEvent(maskCardNumber(number), email, userId);
//    }

    public CardStatus convertStringToCardStatusAction(String inputString) {
        try {
            return CardStatus.valueOf(inputString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CardStatusActionException("Invalid card action: " + inputString);
        }
    }

//    @Transactional
//    public void removeCard(String number, UUID userId) {
//        if (!isCardLinkedToUser(number, userId)) {
//            throw new CardAccessDeniedException("You can't remove someone's card");
//        }
//        cardRepository.deleteCardByNumber(number);
//    }

//    public CardStatusDto getCardStatus(String number) {
//        Card card = getCardByNumber(number);
//        return new CardStatusDto(
//                card.isFrozen(),
//                card.isBlocked(),
//                cardDataValidator.isCardExpired(card.getExpirationDate())
//        );
//    }

//    @Transactional
//    public void subtractMoney(UUID userId, BigDecimal amount, String cardNumber) {
//        Card card = getCardByNumber(cardNumber);
//
//        if (!isCardLinkedToUser(cardNumber, userId)) {
//            throw new CardAccessDeniedException("You can't subtract money from someone's card");
//        }
//
//        if (card.getMoney().compareTo(amount) < 0) {
//            throw new InsufficientBalanceException("Insufficient balance");
//        }
//        card.setMoney(card.getMoney().subtract(amount));
//        cardRepository.save(card);
//    }
}
