// UserGrpcClient.java
package quant.orderservice.client;

import com.example.grpc.user.*;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import quant.orderservice.exception.AppException;
import quant.orderservice.exception.ErrorCode;

@Slf4j
@Component
public class UserGrpcClient {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    public UserResponse getUser(String userId) {
        try {
            return userStub.getUser(
                    GetUserRequest.newBuilder()
                            .setUserId(userId)
                            .build());
        } catch (StatusRuntimeException e) {
            log.error("gRPC getUser error: {}", e.getMessage());
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
    }
}