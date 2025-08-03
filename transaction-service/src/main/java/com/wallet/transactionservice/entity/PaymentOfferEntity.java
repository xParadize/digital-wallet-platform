package com.wallet.transactionservice.entity;

import com.wallet.transactionservice.enums.Currency;
import com.wallet.transactionservice.enums.TransactionCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment_offer")
public class PaymentOfferEntity {
    @Id
    private String id;

    @Column
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    private TransactionCategory category;

    @Column
    private String vendor;

    @Column
    private float latitude;

    @Column
    private float longitude;

    @Column
    private Instant suggestedAt;

    @Column
    private Instant completedAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        PaymentOfferEntity that = (PaymentOfferEntity) o;
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
                "amount = " + amount + ", " +
                "currency = " + currency + ", " +
                "category = " + category + ", " +
                "vendor = " + vendor + ", " +
                "latitude = " + latitude + ", " +
                "longitude = " + longitude + ", " +
                "suggestedAt = " + suggestedAt + ", " +
                "completedAt = " + completedAt + ")";
    }
}
