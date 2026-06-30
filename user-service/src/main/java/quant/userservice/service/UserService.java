package quant.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import quant.userservice.dto.request.UpdateProfileRequest;
import quant.userservice.dto.response.UserProfileResponse;
import quant.userservice.entity.UserProfile;
import quant.userservice.exception.AppException;
import quant.userservice.exception.ErrorCode;
import quant.userservice.repository.UserProfileRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;

    @Transactional
    public void createProfile(String id, String email) {
        if (userProfileRepository.existsById(id)) return;
        UserProfile profile = UserProfile.builder()
                .id(id)
                .email(email)
                .build();
        userProfileRepository.save(profile);
    }

    public UserProfileResponse getProfile(String id) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return toResponse(profile);
    }

    public UserProfileResponse getProfileByEmail(String email) {
        UserProfile profile = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return toResponse(profile);
    }

    @Transactional
    public UserProfileResponse updateProfile(String id, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());

        return toResponse(userProfileRepository.save(profile));
    }

    private UserProfileResponse toResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .id(profile.getId())
                .email(profile.getEmail())
                .fullName(profile.getFullName())
                .phone(profile.getPhone())
                .address(profile.getAddress())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
