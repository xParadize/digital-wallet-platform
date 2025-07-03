package com.wallet.authservice.feign;

import com.wallet.authservice.entity.UnverifiedUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "user-client",
        url = "http://localhost:8082")
public interface UnverifiedUserClient {

    @PostMapping("/api/v1/user")
    ResponseEntity<HttpStatus> saveUser(@RequestBody UnverifiedUser unverifiedUser);

    @GetMapping("/api/v1/user/exists")
    boolean existsByEmailOrPhone(@RequestParam("email") String email, @RequestParam("phone") String phone);
}
