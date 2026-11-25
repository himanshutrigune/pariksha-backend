package com.pariksha.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;

@Configuration
@EnableJpaAuditing
public class AppConfig {

    /** Injecting a Clock (instead of calling LocalDateTime.now() everywhere) makes time-based logic testable. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
