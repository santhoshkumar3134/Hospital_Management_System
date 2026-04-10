package com.hospital.appointmentservice.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Feign client logging.
 * Enables detailed logging of all Feign requests and responses.
 */
@Configuration
public class FeignLoggingConfig {

    /**
     * Set Feign logger level to FULL for comprehensive request/response logging.
     * FULL logs request and response headers, body, and metadata.
     * 
     * @return Logger level FULL
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
