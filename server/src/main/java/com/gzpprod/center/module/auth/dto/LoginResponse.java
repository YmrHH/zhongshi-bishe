package com.gzpprod.center.module.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String token;
    private String username;
    private String role;
    private String roleLabel;
    private String realName;
    private String orgName;
    private String homePath;
}
