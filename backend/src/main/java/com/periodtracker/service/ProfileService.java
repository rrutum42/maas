package com.periodtracker.service;

import com.periodtracker.dto.ProfileRequest;
import com.periodtracker.dto.ProfileResponse;
import com.periodtracker.entity.UserProfile;
import com.periodtracker.exception.NotFoundException;
import com.periodtracker.repository.UserProfileRepository;
import com.periodtracker.repository.UserRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public ProfileService(UserProfileRepository userProfileRepository,
                          UserRepository userRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    public ProfileResponse upsertProfile(ProfileRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new NotFoundException("User not found with id: " + request.userId());
        }

        UserProfile profile = userProfileRepository.findById(request.userId())
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUserId(request.userId());
                    return p;
                });

        profile.setTypicalCycleLengthDays(request.typicalCycleLengthDays());
        profile.setTypicalPeriodDurationDays(request.typicalPeriodDurationDays());
        profile.setLastPeriodStartDate(request.lastPeriodStartDate());
        profile.setOnboardingCompletedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());

        userProfileRepository.save(profile);
        return toResponse(profile);
    }

    public ProfileResponse getProfile(Long userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Profile not found for user: " + userId));
        return toResponse(profile);
    }

    private ProfileResponse toResponse(UserProfile profile) {
        return new ProfileResponse(
                profile.getUserId(),
                profile.getTypicalCycleLengthDays(),
                profile.getTypicalPeriodDurationDays(),
                profile.getLastPeriodStartDate(),
                profile.isOnboardingCompleted(),
                profile.getOnboardingCompletedAt()
        );
    }
}
