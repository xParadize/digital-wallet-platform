package com.wallet.analyticsservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "expense_analysis_report")
public class ExpenseAnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String cardNumber;

    @Column
    private LocalDate periodFrom;

    @Column
    private LocalDate periodTo;

    @Column
    private String report;

    @Column
    private Instant createdAt;

    @Column
    private Instant requestedAt;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        ExpenseAnalysisReport that = (ExpenseAnalysisReport) o;
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
                "cardNumber = " + cardNumber + ", " +
                "from = " + periodFrom + ", " +
                "to = " + periodTo + ", " +
                "report = " + report + ", " +
                "createdAt = " + createdAt + ", " +
                "requestedAt = " + requestedAt + ")";
    }
}
