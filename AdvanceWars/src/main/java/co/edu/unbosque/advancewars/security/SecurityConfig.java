package co.edu.unbosque.advancewars.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

/**
 * Configura autenticación, BCrypt, rutas protegidas y CORS.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * Proporciona el codificador de contraseñas BCrypt configurable.
     */
    @Bean
    public PasswordEncoder passwordEncoder(@Value("${app.security.bcrypt.strength}") int bcryptStrength) {
        if (bcryptStrength < 10 || bcryptStrength > 14) {
            throw new IllegalArgumentException("BCrypt strength debe estar entre 10 y 14");
        }
        return new BCryptPasswordEncoder(bcryptStrength);
    }

    /**
     * Configura el proveedor de autenticación mediante usuarios persistidos.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Proporciona el gestor de autenticación de Spring Security.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    /**
     * Restringe los orígenes y encabezados admitidos por CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origin}") String allowedOrigin,
            @Value("${app.cors.max-age-seconds}") long maxAgeSeconds) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(allowedOrigin));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        cors.setExposedHeaders(List.of("Content-Disposition"));
        cors.setAllowCredentials(false);
        cors.setMaxAge(maxAgeSeconds);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    /**
     * Define sesiones sin estado, validación JWT y permisos por ruta.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            JwtUtil jwtUtil, UserDetailsService userDetailsService,
            AuthenticationProvider authenticationProvider,
            @Value("${app.openapi.public}") boolean openApiPublic) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .accessDeniedHandler((request, response, exception) ->
                    response.sendError(HttpServletResponse.SC_FORBIDDEN)))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService),
                    UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authorize -> {
                authorize.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();
                authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                authorize.requestMatchers(HttpMethod.POST,
                        "/api/auth/register", "/api/auth/verify-email", "/api/auth/login",
                        "/api/auth/verify-otp", "/api/auth/resend-otp",
                        "/api/auth/forgot-password", "/api/auth/reset-password").permitAll();
                if (openApiPublic) {
                    authorize.requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                            "/v3/api-docs", "/v3/api-docs/**").permitAll();
                }
                authorize.requestMatchers("/api/admin/**").hasRole("ADMIN");
                authorize.requestMatchers(HttpMethod.GET, "/api/users/me").authenticated();
                authorize.requestMatchers("/api/users/**").hasRole("ADMIN");
                authorize.anyRequest().authenticated();
            });
        return http.build();
    }
}
