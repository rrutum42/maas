package com.periodtracker.controller;

import com.periodtracker.dto.ProfileRequest;
import com.periodtracker.dto.ProfileResponse;
import com.periodtracker.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> upsert(@Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.upsertProfile(request));
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> get(@RequestParam Long userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }
}
