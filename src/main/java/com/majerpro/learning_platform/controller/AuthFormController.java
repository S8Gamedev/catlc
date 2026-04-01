package com.majerpro.learning_platform.controller;

import com.majerpro.learning_platform.dto.UserRegistrationDto;
import com.majerpro.learning_platform.dto.ui.UiRegisterForm;
import com.majerpro.learning_platform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthFormController {

    private final UserService userService;

    public AuthFormController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public String registerFromUi(@Valid @ModelAttribute("form") UiRegisterForm form, BindingResult br) {
        if (br.hasErrors()) return "register";

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            br.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
            return "register";
        }

        UserRegistrationDto dto = new UserRegistrationDto();
        // Use whatever your UserRegistrationDto supports:
        dto.setEmail(form.getEmail());
        dto.setPassword(form.getPassword());

        // If your UserRegistrationDto has username:
        dto.setUsername(form.getUsername()); // keep this line if it compiles

        // If it DOES NOT have username but has name instead, replace with:
        // dto.setName(form.getUsername());

        userService.registerUser(dto);
        return "redirect:/login?success";
    }
}
