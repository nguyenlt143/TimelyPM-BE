package com.CapstoneProject.capstone.config;

import com.CapstoneProject.capstone.controller.GoogleAuthenticationController;
import com.CapstoneProject.capstone.dto.response.auth.CustomAccessTokenResponseClient;
import com.CapstoneProject.capstone.exception.CustomAccessDeniedHandler;
import com.CapstoneProject.capstone.exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Optional;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);
    private final JwtFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAccessDeniedHandler customAccessDeniedHandler, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/register", "/api/user/auth", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/user/google-auth/**").permitAll()
                        .requestMatchers("/comment").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPoint))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth.baseUri("/oauth2/authorization"))
                        .redirectionEndpoint(redir -> redir.baseUri("/api/user/google-auth/signin-google"))
                        .userInfoEndpoint(userInfo -> userInfo.oidcUserService(new OidcUserService() {
                            @Override
                            public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
                                logger.info("Loading OidcUser with access token: {}", userRequest.getAccessToken().getTokenValue());
                                OidcUser user = super.loadUser(userRequest);
                                logger.info("Loaded OidcUser attributes: {}", user.getAttributes());
                                logger.info("User email: {}", Optional.ofNullable(user.getAttribute("email")));
                                logger.info("User sub: {}", Optional.ofNullable(user.getAttribute("sub")));
                                return user;
                            }
                        }))
                        .successHandler((request, response, authentication) -> {
                            logger.info("OAuth2 login success: Principal={}", authentication.getPrincipal());
                            response.sendRedirect("/api/user/google-auth/success");
                        })
                        .failureHandler((request, response, exception) -> {
                            logger.error("OAuth2 login failed: {}, State={}", exception.getMessage(), request.getParameter("state"), exception);
                            response.sendRedirect("/api/user/google-auth/login?error=true");
                        })
                );
        return http.build();
    }
}