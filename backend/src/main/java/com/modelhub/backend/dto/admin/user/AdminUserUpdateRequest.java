package com.modelhub.backend.dto.admin.user;

import jakarta.validation.constraints.Size;

public class AdminUserUpdateRequest {
    @Size(max = 64, message = "nickname too long")
    private String nickname;

    @Size(max = 64, message = "real name too long")
    private String realName;

    @Size(max = 32, message = "mobile too long")
    private String mobile;

    @Size(max = 128, message = "email too long")
    private String email;

    private Long orgId;

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

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }
}
