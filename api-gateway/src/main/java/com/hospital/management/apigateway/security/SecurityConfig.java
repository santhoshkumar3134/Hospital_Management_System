package com.hospital.management.apigateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter; // 1. Inject your custom reactive filter

    @Bean // 2. Essential: Register this as a Spring Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {

        httpSecurity
                .authorizeExchange(exchanges -> exchanges
                        // 3. Use pathMatchers for WebFlux and include the leading slash
                        .pathMatchers("/patient-service/**").authenticated()
                        // 4. It is good practice to explicitly state what happens to everything else
                        .anyExchange().permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable()) // Disable basic auth as we use JWT

                // 5. Register your custom JwtFilter in the reactive chain
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return httpSecurity.build();
    }
}