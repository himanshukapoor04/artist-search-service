package com.mashup.music.search.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Class for carrying spring security specific properties like password.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties("spring.security.user")
public class SpringSecurityProperties {

    @NotNull
    private String password;
}
