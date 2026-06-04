package codewithmoise.org.blogbackend.security;

import codewithmoise.org.blogbackend.config.WebConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final WebConfig webConfig;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, WebConfig webConfig) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.webConfig = webConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(webConfig.corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // public read
                        .requestMatchers(HttpMethod.GET, "/api/blogs/**").permitAll()
                        .requestMatchers("/api/categories", "/api/tags").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()


                        .requestMatchers("/actuator/heath").permitAll()

                        // authors manage blogs
                        .requestMatchers(HttpMethod.POST,   "/api/blogs/**").hasRole("AUTHOR")
                        .requestMatchers(HttpMethod.PUT,    "/api/blogs/**").hasRole("AUTHOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/blogs/**").hasRole("AUTHOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/blogs/**").hasRole("AUTHOR")

                        // admin monitoring
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}