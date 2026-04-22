// What fields can be updated — password and email only
// Username and role changes are sensitive — handled separately
package com.pms.auth.dto;

import com.pms.auth.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequestDTO {

    @Email(message = "Invalid email format")
    private String email;          // optional — only update if provided

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;    // optional — only update if provided

    private Role role;
}