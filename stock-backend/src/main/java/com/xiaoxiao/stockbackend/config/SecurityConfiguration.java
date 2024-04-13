package com.xiaoxiao.stockbackend.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

@Configuration
public class SecurityConfiguration {


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf -> conf
                                .anyRequest().permitAll()
                )
                .formLogin(conf -> conf
                        .loginProcessingUrl("/api/auth/login") // 表单提交地址
                        .successHandler(this::onAuthenticationSuccess)
                        .failureHandler(new AuthenticationFailureHandler() {
                            @Override
                            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

                            }
                        })
                        .permitAll()
                )
                .logout(conf -> conf
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new LogoutSuccessHandler() {
                            @Override
                            public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

                            }
                        })
                )
//                .exceptionHandling(conf -> conf
//                        .
//                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(conf -> conf
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .build();
    }

    private void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

    }
}
