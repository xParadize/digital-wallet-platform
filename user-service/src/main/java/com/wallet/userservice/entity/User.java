package com.wallet.userservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_")
public class User {
    @Id
    private UUID id;
    private String name;
    private String lastname;
    private String patronymic;
    private String birthDate;
    private String phone;
    private String email;
}
