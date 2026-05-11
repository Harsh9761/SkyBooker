package com.example.api_gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;

    @Autowired
    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //Allow preflight requests
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();

        // Public APIs
        
        if (path.startsWith("/oauth2") || path.startsWith("/login")) {
            return chain.filter(exchange);
        }
        
        if (
                path.startsWith("/auth/login") ||
                path.startsWith("/auth/register") ||
                path.startsWith("/auth/send-otp") ||
                path.startsWith("/auth/verify-otp") ||

                
                path.startsWith("/auth/send-register-otp") ||
                path.startsWith("/auth/verify-register-otp") ||

                (path.startsWith("/flights") &&
                        exchange.getRequest().getMethod() == HttpMethod.GET)
        ) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.validate(token)) {
            return unauthorized(exchange);
        }

        String role = jwtUtil.getRole(token);

        // RBAC
        if (path.startsWith("/airports") || path.startsWith("/airlines")) {
            if (!"ADMIN".equals(role)) return forbidden(exchange);
        }

        if (path.startsWith("/bookings") || path.startsWith("/payments")) {
            if (!("PASSENGER".equals(role) || "ADMIN".equals(role))) {
                return forbidden(exchange);
            }
        }

        ServerWebExchange modified = exchange.mutate()
                .request(r -> r
                        .header("X-User", jwtUtil.getUsername(token))
                        .header("X-Role", role)
                        .header("X-User-Id", String.valueOf(jwtUtil.getUserId(token)))
                ).build();

        return chain.filter(modified);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}