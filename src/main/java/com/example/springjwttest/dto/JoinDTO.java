package com.example.springjwttest.dto;

import com.example.springjwttest.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
//@NoArgsConstructor
public class JoinDTO {
    private String username;
    private String password;

    /*@Builder
    public JoinDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }*/

    public UserEntity toUserEntity(String username, String password, String role) {
        return UserEntity.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();
    }
}
