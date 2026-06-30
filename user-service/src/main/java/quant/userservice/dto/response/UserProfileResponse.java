package quant.userservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class UserProfileResponse {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String address;
    private Instant createdAt;
    private Instant updatedAt;
}
