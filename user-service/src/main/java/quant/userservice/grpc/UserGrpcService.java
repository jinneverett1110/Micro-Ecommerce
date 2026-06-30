package quant.userservice.grpc;

import com.example.grpc.user.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import quant.userservice.dto.response.UserProfileResponse;
import quant.userservice.exception.AppException;
import quant.userservice.service.UserService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {
    private final UserService userService;

    @Override
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserProfileResponse profile = userService.getProfile(request.getUserId());
            responseObserver.onNext(toProto(profile));
            responseObserver.onCompleted();
        } catch (AppException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getUserByEmail(GetUserByEmailRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UserProfileResponse profile = userService.getProfileByEmail(request.getEmail());
            responseObserver.onNext(toProto(profile));
            responseObserver.onCompleted();
        } catch (AppException e) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {
        try {
            userService.createProfile(request.getUserId(), request.getEmail());
            responseObserver.onNext(CreateUserResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error creating user profile: ", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Lỗi tạo user profile")
                    .asRuntimeException());
        }
    }

    private UserResponse toProto(UserProfileResponse profile) {
        return UserResponse.newBuilder()
                .setId(profile.getId())
                .setEmail(profile.getEmail())
                .setFullName(profile.getFullName() != null ? profile.getFullName() : "")
                .setPhone(profile.getPhone() != null ? profile.getPhone() : "")
                .setAddress(profile.getAddress() != null ? profile.getAddress() : "")
                .build();
    }
}
