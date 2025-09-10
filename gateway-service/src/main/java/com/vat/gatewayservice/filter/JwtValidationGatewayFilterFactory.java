package com.vat.gatewayservice.filter;

import com.vat.gatewayservice.util.JwtUtils;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtValidationGatewayFilterFactory.Config> {

    public static final Logger logger = LoggerFactory.getLogger(JwtValidationGatewayFilterFactory.class);
    private final JwtUtils jwtUtils;

    public JwtValidationGatewayFilterFactory(JwtUtils jwtUtils) {
        super(Config.class);
        this.jwtUtils = jwtUtils;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String requestPath = exchange.getRequest().getPath().toString();
            logger.info("Request path: {}", requestPath);

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Unauthorized Access Denied for request url: {}", requestPath);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                String token = authHeader.substring(7);

                // Validate JWT token
                jwtUtils.validateToken(token);
                logger.info("Token validated successfully for path: {}", requestPath);

                // Check admin role if required
                if (config.requireAdmin) {
                    String role = jwtUtils.getRoleFromToken(token);
                    if (!"ADMIN".equals(role)) {
                        logger.warn("Access denied. Admin role required for path: {} - User role: {}", requestPath, role);
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                    logger.info("Admin access granted for path: {}", requestPath);
                }

                return chain.filter(exchange)
                        .doOnSuccess(result -> logger.info("Request completed: {} - status: {}",
                                                           requestPath, exchange.getResponse().getStatusCode()))
                        .doOnError(error -> logger.error("Request failed: {} - error: {}",
                                                         requestPath, error.getMessage()));

            } catch (JwtException e) {
                logger.error("JWT validation failed for path: {} - error: {}", requestPath, e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } catch (Exception e) {
                logger.error("Unexpected error for path: {} - error: {}", requestPath, e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                return exchange.getResponse().setComplete();
            }
        };
    }

    public static class Config {
        private boolean requireAdmin = false;

        public boolean isRequireAdmin() {
            return requireAdmin;
        }

        public void setRequireAdmin(boolean requireAdmin) {
            this.requireAdmin = requireAdmin;
        }
    }
}
