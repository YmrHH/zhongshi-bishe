package com.gzpprod.center.module.auth.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {

    private String phone;
    private String oldPassword;
    private String newPassword;
}
