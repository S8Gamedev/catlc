package com.majerpro.learning_platform.dto.ui;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UiRegisterForm {

    @NotBlank
    private String username;   // use username since your repo supports it

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;
}
