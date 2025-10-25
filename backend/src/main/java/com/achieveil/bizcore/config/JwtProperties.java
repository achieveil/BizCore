package com.achieveil.bizcore.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * Shared secret for signing JWT tokens. Should be at least 256 bits.
     */
    private String secret = "BizCoreChangeMeSecretKeyBizCoreChangeMeSecretKey";

    /**
     * Access token lifetime in seconds.
     */
    private long accessTokenTtl = 3600;

    /**
     * Refresh token lifetime in seconds.
     */
    private long refreshTokenTtl = 604800;
}
