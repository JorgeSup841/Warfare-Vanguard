package co.edu.unbosque.advancewars.security;

import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Examina tokens Bearer por petición y configura la identidad autenticada.
 * Solo establece el contexto si la firma y el estado del usuario son válidos.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final UserSessionService sessions;
    private final UserDetailsService userDetailsService;

    /**
     * Inicializa el verificador JWT y el servicio que consulta usuarios actuales.
     *
     * @param jwtUtil utilidad de creación y validación de JWT
     * @param userDetailsService recupera usuarios desde el almacenamiento
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService,
            UserSessionService sessions) {
        this.jwtUtil = jwtUtil;
        this.sessions = sessions;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        User authenticated = null;
        String sessionId = null;
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtUtil.parse(authorization.substring(7));
                User user = (User) userDetailsService.loadUserByUsername(claims.getSubject());
                if (jwtUtil.validateClaims(claims, user) && sessions.isAllowed(user, claims.getId())) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    authenticated = user;
                    sessionId = claims.getId();
                    sessions.touch(user, sessionId);
                }
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException | ClassCastException exception) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);

        // Solo registrar acciones con resultado exitoso, no cada consulta GET ni heartbeat.
        if (authenticated != null && sessionId != null && response.getStatus() >= 200
                && response.getStatus() < 400 && isChangeRequest(request)) {
            String action = request.getMethod() + " " + request.getRequestURI();
            if (action.length() > 180) {
                action = action.substring(0, 180);
            }
            try {
                sessions.recordAction(authenticated, sessionId, action);
            } catch (RuntimeException exception) {
                // El historial no debe transformar una jugada exitosa en un error HTTP.
                log.warn("No se pudo registrar la accion de {}", authenticated.getUsername(), exception);
            }
        }
    }

    private boolean isChangeRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (path.startsWith("/api/sessions/") || path.startsWith("/api/auth/")) {
            return false;
        }
        return method.equals("POST") || method.equals("PATCH")
                || method.equals("PUT") || method.equals("DELETE");
    }
}
