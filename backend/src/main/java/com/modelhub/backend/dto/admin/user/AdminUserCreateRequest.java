package com.modelhub.backend.dto.admin.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class AdminUserCreateRequest {
    @NotBlank(message = "username is required")
    @Size(max = 64, message = "username too long")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, max = 128, message = "password length must be between 6 and 128")
    private String password;

    @Size(max = 64, message = "nickname too long")
    private String nickname;

    @Size(max = 64, message = "real name too long")
    private String realName;

    @Size(max = 32, message = "mobile too long")
    private String mobile;

    @Size(max = 128, message = "email too long")
    private String email;

    private Integer status;
    private Long orgId;
    private List<Long> roleIds = new ArrayList<>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
