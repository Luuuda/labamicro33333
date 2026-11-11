package org.example.gateway.application;

import java.util.Optional;

public record ServiceResult<T>(String serviceName, Optional<T> data, String message) {

    public static <T> ServiceResult<T> available(String serviceName, T data) {
        return new ServiceResult<>(serviceName, Optional.ofNullable(data), "ok");
    }

    public static <T> ServiceResult<T> unavailable(String serviceName, String message) {
        return new ServiceResult<>(serviceName, Optional.empty(), message);
    }
}
