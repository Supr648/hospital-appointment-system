package com.hospital.appointmentsystem.controller;

import com.hospital.appointmentsystem.service.RegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult bindingResult,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please correct the highlighted registration details.");
            return "register";
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("error", "The passwords do not match.");
            return "register";
        }
        try {
            registrationService.register(form.getUsername(), form.getPassword(), form.getOrganizationName());
        } catch (RegistrationService.AccountAlreadyExistsException exception) {
            model.addAttribute("error", "An account with this email already exists. Please sign in.");
            return "register";
        }
        return "redirect:/login?registered";
    }

    public static class RegistrationForm {
        @NotBlank
        @Email
        @Size(max = 190)
        private String username;

        @NotBlank
        @Size(min = 12, max = 72)
        private String password;

        @NotBlank
        @Size(min = 12, max = 72)
        private String confirmPassword;

        @NotBlank
        @Size(max = 120)
        private String organizationName;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    }
}
