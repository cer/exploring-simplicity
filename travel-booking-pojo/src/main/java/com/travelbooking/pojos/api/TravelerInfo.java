package com.travelbooking.pojos.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TravelerInfo(
    @NotBlank(message = "Name is required")
    String name,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email
) {
}