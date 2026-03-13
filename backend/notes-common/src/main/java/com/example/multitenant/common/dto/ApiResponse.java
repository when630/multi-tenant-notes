package com.example.multitenant.common.dto;

import com.example.multitenant.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
  
  private final String code;
  private final T data;
  private final String message;
  
  private ApiResponse(String code, T data, String message) {
    this.code = code;
    this.data = data;
    this.message = message;
  }
  
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>("SUCCESS", data, null);
  }
  
  public static ApiResponse<Void> success() {
    return new ApiResponse<>("SUCCESS", null, null);
  }
  
  public static ApiResponse<Void> error(ErrorCode errorCode) {
    return new ApiResponse<>(errorCode.name(), null, errorCode.getMessage());
  }
  
  public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
    return new ApiResponse<>(errorCode.name(), null, message);
  }
}
