package com.modelhub.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SendContactVerifyCodeRequest {
    @NotBlank(message = "type is required")
    @Pattern(regexp = "EMAIL|MOBILE", message = "type must be EMAIL or MOBILE")
    private String type;

    @NotBlank(message = "target is required")
    private String target;

    private String captchaToken;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }
}
