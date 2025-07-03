package com.wallet.userservice.controller;

import com.wallet.userservice.entity.UnverifiedUser;
import com.wallet.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserApiController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<HttpStatus> saveUser(@RequestBody UnverifiedUser unverifiedUser) {
        userService.saveUser(unverifiedUser);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/exists")
    public boolean userExists(@RequestParam("email") String email, @RequestParam("phone") String phone) {
        return userService.existsByEmailOrPhone(email, phone);
    }
}

