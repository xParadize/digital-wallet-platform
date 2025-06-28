package com.wallet.authservice.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.UUID;

@RedisHash(value = "UnverifiedUser", timeToLive = 900)
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnverifiedUser implements Serializable {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;
    private String name;
    private String lastname;
    private String patronymic;
    private String birthDate;
    private String phone;
    private String email;
    private String password;
}
