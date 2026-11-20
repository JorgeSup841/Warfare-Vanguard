package co.edu.unbosque.advancewars.security;

import jakarta.servlet.DispatcherType;
import java.util.List;
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

/**
 * Configura autenticación sin sesión, JWT, BCrypt, CORS y autorización HTTP.
 * Los parámetros de seguridad se obtienen de application.properties.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthFilter;
	private final UserDetailsService userDetailsService;

	/**
	 * Inicializa el filtro JWT y la búsqueda de identidades.
	 *
	 * @param jwtUtil            servicio de tokens firmados
	 * @param userDetailsService consulta usuarios al autenticar peticiones
	 */
	public SecurityConfig(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
		this.jwtAuthFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Crea el codificador BCrypt con el factor de costo configurado.
	 *
	 * @param bcryptStrength factor de costo, entre 10 y 14
	 * @return codificador de contraseñas BCrypt
	 * @throws IllegalArgumentException si el factor está fuera del rango
	 */
	@Bean
	public PasswordEncoder passwordEncoder(@Value("${app.security.bcrypt.strength}") int bcryptStrength) {
		if (bcryptStrength < 10 || bcryptStrength > 14) {
			throw new IllegalArgumentException("BCrypt strength debe estar entre 10 y 14");
		}
		return new BCryptPasswordEncoder(bcryptStrength);
	}

	/**
	 * Crea un proveedor de autenticación basado en usuarios y BCrypt.
	 *
	 * @param passwordEncoder codificador empleado para comparar contraseñas
	 * @return proveedor de autenticación DAO
	 */
	@Bean
	public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	/**
	 * Crea el administrador de autenticación con el proveedor DAO.
	 *
	 * @param authenticationProvider proveedor que valida credenciales
	 * @return administrador de autenticación
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
		return new ProviderManager(authenticationProvider);
	}

	/**
	 * Define el origen permitido y las cabeceras CORS para el frontend.
	 *
	 * @param allowedOrigin origen HTTP permitido
	 * @param maxAgeSeconds tiempo de caché de la solicitud previa CORS
	 * @return configuración CORS aplicada a las rutas de la API
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origin}") String allowedOrigin,
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
	 * Protege las rutas privadas y habilita las operaciones de autenticación.
	 *
	 * @param http                   configuración de seguridad HTTP
	 * @param authenticationProvider proveedor de autenticación
	 * @param openApiPublic          permite Swagger sin JWT cuando es verdadero
	 * @return cadena de filtros de seguridad
	 * @throws Exception si no se puede construir la configuración
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider,
			@Value("${app.openapi.public}") boolean openApiPublic) throws Exception {

		http.csrf(csrf -> csrf.disable()).cors(Customizer.withDefaults()).httpBasic(basic -> basic.disable())
				.formLogin(form -> form.disable()).logout(logout -> logout.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(errors -> errors.authenticationEntryPoint((req, res, ex) -> res.sendError(401))
						.accessDeniedHandler((req, res, ex) -> res.sendError(403)))
				.authenticationProvider(authenticationProvider)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> {

					auth.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();

					auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

					auth.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login",
							"/api/auth/verify-email", "/api/auth/verify-otp", "/api/auth/resend-otp",
							"/api/auth/forgot-password", "/api/auth/reset-password").permitAll();

					if (openApiPublic) {
						auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**")
								.permitAll();
					}

					auth.requestMatchers("/api/admin/**").hasRole("ADMIN");
					auth.requestMatchers("/api/users/**").hasRole("ADMIN");

					auth.requestMatchers("/api/auth/me", "/api/users/me", "/api/games", "/api/games/**").authenticated();

					auth.anyRequest().authenticated();
				});

		return http.build();
	}

}