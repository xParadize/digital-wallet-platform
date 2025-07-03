package com.wallet.userservice.entity;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnverifiedUser {
    private UUID id;
    private String name;
    private String lastname;
    private String patronymic;
    private String birthDate;
    private String phone;
    private String email;
    private String password;
}
