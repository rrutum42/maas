package com.periodtracker.service;

import com.periodtracker.dto.UserResponse;
import com.periodtracker.entity.User;
import com.periodtracker.repository.UserProfileRepository;
import com.periodtracker.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public UserService(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private UserResponse toResponse(User user) {
        boolean onboardingCompleted = userProfileRepository.findById(user.getId())
                .map(p -> p.isOnboardingCompleted())
                .orElse(false);
        return new UserResponse(user.getId(), user.getDisplayName(), onboardingCompleted);
    }
}
