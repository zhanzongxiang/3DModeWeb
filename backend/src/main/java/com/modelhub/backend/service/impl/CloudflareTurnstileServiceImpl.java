package com.modelhub.backend.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.config.TurnstileProperties;
import com.modelhub.backend.service.TurnstileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class CloudflareTurnstileServiceImpl implements TurnstileService {
    private final TurnstileProperties turnstileProperties;
    private final RestClient restClient;

    public CloudflareTurnstileServiceImpl(TurnstileProperties turnstileProperties, RestClient.Builder restClientBuilder) {
        this.turnstileProperties = turnstileProperties;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public void verify(String token, String remoteIp) {
        if (!turnstileProperties.isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(turnstileProperties.getSecretKey())) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "turnstile secret key not configured");
        }
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "captcha token required");
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("secret", turnstileProperties.getSecretKey());
        formData.add("response", token);
        if (StringUtils.hasText(remoteIp)) {
            formData.add("remoteip", remoteIp);
        }

        TurnstileVerifyResponse response;
        try {
            response = restClient.post()
                    .uri(turnstileProperties.getVerifyUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(TurnstileVerifyResponse.class);
        } catch (RestClientException ex) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "captcha verification unavailable");
        }

        if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "captcha verification failed");
        }
    }

    public static class TurnstileVerifyResponse {
        private Boolean success;

        @JsonProperty("error-codes")
        private List<String> errorCodes;

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public List<String> getErrorCodes() {
            return errorCodes;
        }

        public void setErrorCodes(List<String> errorCodes) {
            this.errorCodes = errorCodes;
        }
    }
}
