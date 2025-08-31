package com.example.springjwttest.service;

import com.example.springjwttest.dto.CustomUserDetails;
import com.example.springjwttest.entity.UserEntity;
import com.example.springjwttest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
//LoginFilter의 attemptAuthentication 함수에서 authenticationmanager를 호출할 때 실행됨.
//UserData를 검증 후 CustomUserDetails에 전달하는 역할을 함
//authenticate는 DaoAuthenticationProvider 가 알아서 할 것이므로 신경쓰지 않아도 됨.
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userData = userRepository.findByUsername(username);
        if (userData != null) {
             log.info(userData.getUsername());
             return new CustomUserDetails(userData);
        }

        return null;
    }
}
