package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserUpdateDto {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Email(message = "Email should be valid")
    private String email;

    private String firstName;

    private String lastName;

    private List<String> roles;

    public boolean hasUsername() {
        return username != null && !username.trim().isEmpty();
    }

    public boolean hasPassword() {
        return password != null && !password.trim().isEmpty();
    }

    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }

    public boolean hasFirstName() {
        return firstName != null && !firstName.trim().isEmpty();
    }

    public boolean hasLastName() {
        return lastName != null && !lastName.trim().isEmpty();
    }

    public boolean hasRoles() {
        return roles != null && !roles.isEmpty();
    }
}