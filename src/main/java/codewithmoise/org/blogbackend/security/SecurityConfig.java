package codewithmoise.org.blogbackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
              /*  .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))*/

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET,"/api/blogs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/blogs/**").hasRole("AUTHOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/blogs/**").hasRole("AUTHOR")
                        .requestMatchers(HttpMethod.PUT,"/api/blogs/**").hasRole("AUTHOR")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .anyRequest().authenticated()


                );

        return http.build();
    }
}
