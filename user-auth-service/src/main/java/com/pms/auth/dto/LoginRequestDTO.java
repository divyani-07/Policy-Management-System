package com.pms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data  // auto getters/setters
public class LoginRequestDTO {

    @NotBlank(message = "Username is required")  // validation — can't be empty
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
