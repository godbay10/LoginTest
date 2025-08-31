package com.example.springjwttest.controller;

import com.example.springjwttest.entity.RefreshEntity;
import com.example.springjwttest.jwt.JWTUtil;
import com.example.springjwttest.repository.RefreshRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Date;

@Controller
@ResponseBody
public class ReissueController {
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public ReissueController(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        //쿠키가 존재하는지
        if (refresh == null) {
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        //쿠키의 기한이 만료되었는지
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {

            //response status code
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        //DB에 저장되어있는지 확인
        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {

            //response body
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //새로운 access 토큰을 만들어줌
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L);

        //access 토큰 갱신 시 refresh 토큰도 함께 갱신해주는 refresh rotate 방식을 사용해줌
        //refresh 토큰이 새로운 값으로 갱신되기 때문에 저장소에 들어있는 기존 토큰 값은 블랙리스트에 넣어주어야함.
        //이 방식을 사용할 시 refresh 토큰을 매번 갱신해주므로 보안이 강화되지만 그만큼 서버에 부하가 걸리기 쉬움.
        //또한 로직을 훨씬 자세히 구현해야된다는 단점이 있음.
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L);

        //response
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        //지금은 예시라서 그렇지 DB(Repository)에 내용을 추가하고 삭제하는 것은 원래는 service 단에서 코드를 짜고 이를 가져와야 함.
        //@Transactional, 낙관적/비관적 락 등의 동시성 문제를 해결하기 위해서는 service 에서 이를 구현해주어야 함.
        refreshRepository.deleteByRefresh(refresh);
        addRefreshEntity(username, newRefresh, 86400000L);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60); // 12h
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
}
