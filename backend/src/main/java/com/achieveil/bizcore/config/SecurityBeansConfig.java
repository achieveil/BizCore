package com.achieveil.bizcore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        String idForEncode = "argon2";
        Argon2PasswordEncoder argon2 = new Argon2PasswordEncoder(16, 32, 1, 1 << 16, 3);
        PasswordEncoder bcrypt = new BCryptPasswordEncoder();

        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(idForEncode, argon2);
        encoders.put("bcrypt", bcrypt);

        DelegatingPasswordEncoder delegatingPasswordEncoder =
                new DelegatingPasswordEncoder(idForEncode, encoders);
        delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(bcrypt);
        return delegatingPasswordEncoder;
    }
}
