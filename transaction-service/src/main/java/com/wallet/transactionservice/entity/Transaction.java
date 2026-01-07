package com.wallet.transactionservice.entity;

import com.wallet.transactionservice.enums.CardType;
import com.wallet.transactionservice.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transaction_")
public class Transaction {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @Column
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", referencedColumnName = "id")
    private PaymentOfferEntity offer;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column
    private String cardNumber;

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Column
    private BigDecimal fee;

    @Column
    private BigDecimal amount;

    @Column
    private Instant createdAt;

    @Column
    private Instant confirmedAt;

    @Column
    private Instant cancelledAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Transaction that = (Transaction) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "userId = " + userId + ", " +
                "status = " + status + ", " +
                "cardNumber = " + cardNumber + ", " +
                "cardType = " + cardType + ", " +
                "createdAt = " + createdAt + ", " +
                "confirmedAt = " + confirmedAt + ", " +
                "cancelledAt = " + cancelledAt + ")";
    }
}
