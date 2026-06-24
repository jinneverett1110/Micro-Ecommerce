package quant.authservice.client;

import com.example.grpc.user.UserServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class UserServiceGrpcClient {

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    public void createUserProfile(String id, String email) {
        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setUserId(id)
                .setEmail(email)
                .build();
        userServiceStub.createUser(request);
    }
}
