package com.example.user_service.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserCreateRequest {

    @NotBlank
    private String firstname;

    private String lastname;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    // @NotBlank
    private String role; 


    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRoleCode() {
        return role;
    }

    public void setRoleCode(String role) {
        this.role = role;
    }
}
