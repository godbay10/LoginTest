package com.example.springjwttest.dto;

import com.example.springjwttest.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
//등록 시 @AuthenticationPrincipal 등의 기능 사용 가능
public class CustomUserDetails implements UserDetails {

    private final UserEntity userEntity;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //직접 커스텀할때 사용하는 방법
        //동영상에서 알려준 방법
        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return userEntity.getRole();
            }
        });

        return collection;

        //getauthority()가 내부에 구현되어있음.
        //return List.of(new SimpleGrantedAuthority(userEntity.getRole()));
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getUsername();
    }
}
