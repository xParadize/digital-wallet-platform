package com.wallet.cardservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "limit_")
public class Limit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column
    private BigDecimal perTransactionLimit;

    @Column
    private boolean limitEnabled;

    @JsonIgnoreProperties
    @OneToOne(mappedBy = "limit")
    private Card card;
}