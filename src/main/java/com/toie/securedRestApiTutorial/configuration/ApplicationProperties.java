package com.toie.securedRestApiTutorial.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties("security.jwt")
@Data
public class ApplicationProperties {
    private String secretKey;
    private Duration tokenDuration;
}
