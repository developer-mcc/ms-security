package pe.com.mcco.security.infrastructure.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * SEQ_01: Rate limit por IP - 5 req/min en /auth/login.
 * Bucket4j se puede agregar despues; esta implementacion es ligera y sin dependencias extra.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.security.rate-limit-requests-per-minute}")
    private int maxRequests;

    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Instant>> requests = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (!"/auth/login".equals(request.getServletPath())) {
            chain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        Instant now = Instant.now();
        Instant oneMinuteAgo = now.minusSeconds(60);

        ConcurrentLinkedDeque<Instant> timestamps = requests.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());

        // Limpiar entradas viejas
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(oneMinuteAgo)) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxRequests) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json");
            response.getWriter().write("{\"mensaje\": \"Demasiadas solicitudes. Intente en 60 segundos.\"}");
            return;
        }

        timestamps.addLast(now);
        chain.doFilter(request, response);
    }
}
