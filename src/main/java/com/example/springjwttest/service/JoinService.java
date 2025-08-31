package com.example.springjwttest.service;

import com.example.springjwttest.entity.UserEntity;
import com.example.springjwttest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.springjwttest.dto.JoinDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoinService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void joinProcess(JoinDTO joinDTO){
        String username = joinDTO.getUsername();
        String password = joinDTO.getPassword();

        Boolean isExist = userRepository.existsByUsername(username);
        //Repository에 이름이 이미 존재하면 새 계정 등록 불가
        if(isExist){
            return;
        }

        //Repository에 회원 정보 등록, 지금은 만드는 계정마다 전부 ROLE_ADMIN 지급
        UserEntity userEntity = joinDTO.toUserEntity(username, bCryptPasswordEncoder.encode(password), "ROLE_ADMIN");
        userRepository.save(userEntity);
    }
}
