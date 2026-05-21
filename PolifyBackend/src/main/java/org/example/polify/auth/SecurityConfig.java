package org.example.polify.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.polify.common.error.ApiAccessDeniedHandler;
import org.example.polify.common.error.ApiAuthenticationEntryPoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtProperties jwtProperties,
        ObjectMapper objectMapper
    ) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtProperties);

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(fl -> fl.disable())
            .httpBasic(hb -> hb.disable())
            .logout(lo -> lo.disable())
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(new ApiAuthenticationEntryPoint(objectMapper))
                .accessDeniedHandler(new ApiAccessDeniedHandler(objectMapper))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/auth/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/surveys/**", HttpMethod.GET.name())).permitAll()
                // Swagger / OpenAPI should be accessible without auth (dev UX).
                .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui.html")).permitAll()
                // Surveys are public for reads, but creation is moderator-only.
                .requestMatchers(new AntPathRequestMatcher("/error")).permitAll()

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
