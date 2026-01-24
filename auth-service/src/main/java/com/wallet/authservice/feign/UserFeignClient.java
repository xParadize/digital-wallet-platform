package com.wallet.authservice.feign;

import com.wallet.authservice.entity.UnverifiedUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "${digital-wallet-platform.services.user-service.uri}")
public interface UserFeignClient {

    @PostMapping("/api/v1/users")
    ResponseEntity<HttpStatus> saveUser(@RequestBody UnverifiedUser unverifiedUser);

    @GetMapping("/api/v1/users/exists")
    boolean existsByEmailOrPhone(@RequestParam("email") String email, @RequestParam("phone") String phone);
}
