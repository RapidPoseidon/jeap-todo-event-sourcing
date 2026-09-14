package ch.example.todo.task.config;

import ch.admin.bit.jeap.security.resource.token.AuthoritiesResolver;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationConverter;
import ch.admin.bit.jeap.security.resource.validation.JeapJwtDecoderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Overrides the starter's default chain for {@code /api/**}: bearer-token clients carry no cookies, so
 * cookie-based CSRF protection would only reject legitimate cross-origin calls from the UI.
 */
@Configuration
@EnableConfigurationProperties(TodoProperties.class)
@RequiredArgsConstructor
public class WebSecurityConfiguration {

    private final TodoProperties properties;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 20)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                                      JeapJwtDecoderFactory jwtDecoderFactory,
                                                      AuthoritiesResolver authoritiesResolver) throws Exception {
        return http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(requests -> requests.anyRequest().fullyAuthenticated())
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> jwt
                        .decoder(jwtDecoderFactory.createJwtDecoder())
                        .jwtAuthenticationConverter(new JeapAuthenticationConverter(authoritiesResolver))))
                .build();
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 21)
    public SecurityFilterChain apiDocumentationSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
