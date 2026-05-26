package com.elshimma.erp.auth.dto;

import com.elshimma.erp.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private Role role;
}
