package com.mybudget.config;

import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Spring Security의 OncePerRequestFilter를 확장하여
 * JWT 토큰을 검증하고 사용자를 인증하는 역할을 수행
 */
@AllArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private JwtProvider jwtProvider;
    private static final String[] ALL_WHITELIST = {
            "/**/",
            "/swagger-ui/index.html",
            "/swagger-ui/**",
            "/v2/api-docs",
            "/swagger-resources/**",
            "/webjars/**"
    };

    /**
     * 주어진 URI가 화이트리스트에 있는지 여부를 판별
     * @param requestURI 현재 요청의 URI
     * @return 화이트리스트에 포함되어 있지 않으면 true, 포함되어 있으면 false 반환
     */
    private boolean isFilterCheck(String requestURI) {
        return !PatternMatchUtils.simpleMatch(ALL_WHITELIST, requestURI);
    }

    /**
     * HTTP 요청에서 헤더를 통해 JWT 토큰 추출
     * @param request 현재의 HTTP 요청
     * @return 추출된 JWT 토큰 또는 null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 실제 필터 로직이 구현된 메서드로, 토큰을 추출하고 검증하여 사용자 인증,
     * 화이트리스트에 있는 경우 필터를 건너뛰어 다음 필터로 진행
     *
     * @param request 현재의 HTTP 요청
     * @param response HTTP 응답
     * @param chain 다음 필터로 전달하기 위한 FilterChain 객체
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException {

        String token = extractTokenFromRequest(request);

        try {
            // 화이트리스트에 있는 경우에는 필터링을 건너뛰어서 다음 필터로 진행
            if (isFilterCheck(request.getRequestURI())) {
                // 화이트리스트에 없는 경우에만 검증 처리
                if (token != null) {
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(jwtProvider.getAuthentication(token));
                }
            }
            chain.doFilter(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }
}
