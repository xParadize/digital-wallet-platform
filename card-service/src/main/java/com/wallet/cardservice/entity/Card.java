package com.wallet.cardservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "card_")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private UUID userId;

    @Column
    private String number;

    @Column
    private String expirationDate;

    @Column
    private String cvv;

    @Column
    private String cardScheme;

    @Column
    private String cardIssuer;

    @Column
    private BigDecimal money;

    @Column
    private boolean frozen;

    @Column
    private boolean blocked;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "limit_id", referencedColumnName = "id", unique = true)
    private Limit limit;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Card card = (Card) o;
        return getId() != null && Objects.equals(getId(), card.getId());
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
                "number = " + number + ", " +
                "expirationDate = " + expirationDate + ", " +
                "cvv = " + cvv + ", " +
                "cardScheme = " + cardScheme + ", " +
                "cardIssuer = " + cardIssuer + ", " +
                "money = " + money + ", " +
                "frozen = " + frozen + ", " +
                "blocked = " + blocked + ")";
    }
}