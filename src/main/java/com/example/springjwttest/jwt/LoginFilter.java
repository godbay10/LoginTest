package com.example.springjwttest.jwt;

import com.example.springjwttest.dto.CustomUserDetails;
import com.example.springjwttest.entity.RefreshEntity;
import com.example.springjwttest.repository.RefreshRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public LoginFilter(AuthenticationManager authenticationManager,
                       JWTUtil jwtUtil,
                       RefreshRepository refreshRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        log.info(username);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);


        //AuthenticationManager가 authToken의 토큰 타입인 UsernamePasswordAuthenticationToken 을 처리할 수 있는 AuthenticationProvider를 탐색함.
        //여러 AuthenticationProvider 중 이를 처리할 수 있는 DaoAuthenticationProvider 가 내부에서 UserDetailsService를 호출.
        //DaoAuthenticationProvider 에서 authenticate 함수를 알아서 실행시키므로 이 코드는 짤 필요 없음
        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

        String username = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        //이중 토큰 방식
        String accessToken = jwtUtil.createJwt("access", username, role,1000L * 60 * 15);
        String refreshToken = jwtUtil.createJwt("refresh", username, role,1000L * 60 * 60 * 24);

        //86400000L
        addRefreshEntity(username, refreshToken, 1000L * 60 * 60 * 24);

        response.setHeader("access", accessToken);
        response.addCookie(createCookie("refresh", refreshToken));
        response.setStatus(HttpStatus.OK.value());
    }


    private Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60); // 12h
        cookie.setHttpOnly(true);   //JS로 접근 불가, 탈취 위험 감소
        return cookie;
    }

    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }
}
