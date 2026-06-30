# Ecom Microservices

Hệ thống thương mại điện tử được thiết kế và xây dựng theo kiến trúc microservices, gồm 9 dịch vụ độc lập, sử dụng Spring Boot, Spring Cloud, PostgreSQL, gRPC và Kafka để xử lý giao tiếp đồng bộ lẫn bất đồng bộ giữa các service.

## Kiến trúc tổng quan

Dự án gồm các module chính sau:

- **service-discovery**: Eureka Server, đóng vai trò registry để các service đăng ký và tra cứu lẫn nhau.
- **config-server**: Spring Cloud Config Server, quản lý cấu hình tập trung cho toàn hệ thống.
- **api-gateway**: Triển khai bằng Spring Cloud Gateway, là cổng vào duy nhất của hệ thống, định tuyến request đến các service nội bộ và xác thực JWT (RS256).
- **auth-service**: Cấp phát JWT (access token, refresh token); lưu trữ khóa riêng (private key) để ký token bằng thuật toán RS256.
- **user-service**: Quản lý thông tin người dùng.
- **product-service**: Quản lý thông tin sản phẩm.
- **order-service**: Quản lý đơn hàng, phát sinh sự kiện `ORDER_CREATED` lên Kafka khi đơn hàng được tạo.
- **payment-service**: Lắng nghe sự kiện `ORDER_CREATED`, xử lý thanh toán và phát sinh sự kiện `PAYMENT_SUCCESS` khi thanh toán hoàn tất.
- **notification-service**: Lắng nghe sự kiện `PAYMENT_SUCCESS` từ Kafka và gửi email xác nhận cho khách hàng.
- **grpc-proto**: Chứa các file `.proto` định nghĩa hợp đồng giao tiếp gRPC dùng chung giữa các service.

## Công nghệ sử dụng

- **Ngôn ngữ**: Java
- **Framework**: Spring Boot, Spring Cloud (Gateway, Config, Eureka)
- **Giao tiếp nội bộ (đồng bộ)**: gRPC + Protocol Buffers, kết hợp Eureka để resolve địa chỉ qua `discovery:///`, giúp giảm độ trễ so với REST truyền thống
- **Giao tiếp bất đồng bộ (event-driven)**: Apache Kafka — order-service, payment-service, notification-service giao tiếp với nhau qua các topic sự kiện thay vì gọi trực tiếp
- **Giao tiếp ngoài**: REST API qua API Gateway
- **Service Discovery**: Eureka Server (port 8761)
- **Cấu hình tập trung**: Spring Cloud Config Server
- **Cơ sở dữ liệu**: PostgreSQL
- **Xác thực**: JWT theo thuật toán RS256 — khóa riêng (private key) được lưu trữ tại `auth-service` để ký token, khóa công khai (public key) được phân phối cho các service khác (qua API Gateway) để xác thực chữ ký mà không cần gọi ngược lại `auth-service`
- **Containerization**: Docker
- **Build tool**: Maven (kèm Maven Wrapper)

## Luồng xử lý sự kiện (event-driven flow)

Ví dụ luồng đặt hàng và thanh toán theo hướng sự kiện qua Kafka:

1. Khách hàng tạo đơn hàng → `order-service` lưu đơn hàng và phát sinh sự kiện `ORDER_CREATED` lên Kafka.
2. `payment-service` lắng nghe topic chứa `ORDER_CREATED`, thực hiện xử lý thanh toán.
3. Khi thanh toán thành công, `payment-service` phát sinh sự kiện `PAYMENT_SUCCESS` lên Kafka.
4. `notification-service` lắng nghe `PAYMENT_SUCCESS` và gửi email xác nhận đơn hàng cho khách hàng.

Cách tiếp cận này giúp các service không phụ thuộc trực tiếp (loose coupling), tăng khả năng chịu lỗi và mở rộng theo chiều ngang.

## Cơ chế xác thực JWT (RS256)

- `auth-service` giữ private key, dùng để ký (sign) access token/refresh token theo thuật toán RS256.
- Public key tương ứng được phân phối cho `api-gateway` (và các service khác nếu cần xác thực trực tiếp) để verify chữ ký token mà không cần lưu trữ secret dùng chung (khác với HS256).
- `api-gateway` là nơi xác thực JWT tập trung trước khi định tuyến request đến các service nội bộ.

## Cấu trúc thư mục

```
ecom-microservices/
├── .mvn/wrapper/
├── api-gateway/
├── auth-service/
├── config-server/
├── grpc-proto/
├── notification-service/
├── order-service/
├── payment-service/
├── product-service/
├── service-discovery/
├── user-service/
├── .gitattributes
├── .gitignore
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## Yêu cầu hệ thống

- JDK 17+ (hoặc phiên bản phù hợp với pom.xml)
- Maven 3.8+ (hoặc dùng `mvnw` đi kèm)
- PostgreSQL
- Các biến môi trường nhạy cảm (DB credentials, JWT key, v.v.) được externalize qua file `.env` hoặc biến môi trường hệ thống

## Hướng dẫn chạy dự án

1. Khởi động **service-discovery** (Eureka Server) trước tiên.
2. Khởi động **config-server**.
3. Khởi động các service nghiệp vụ: `auth-service`, `user-service`, `product-service`, `order-service`, `payment-service`, `notification-service`.
4. Khởi động **api-gateway** sau cùng để định tuyến request vào hệ thống.

> Lưu ý: Khi chạy trên IntelliJ, cần đảm bảo **Working Directory** trong Run Configuration được trỏ đúng vào thư mục của từng service để việc đọc file `.env` hoạt động chính xác.

## Ghi chú

- `auth-service` chỉ đảm nhiệm việc **cấp phát** JWT; việc **xác thực** JWT được thực hiện tại `api-gateway`.
- Các service sử dụng gRPC cần tích hợp `net.devh:grpc-server-spring-boot-starter` để đảm bảo Eureka có thể resolve đúng cổng gRPC khi dùng `discovery:///`.
