package com.example.springjwttest.repository;

import com.example.springjwttest.entity.RefreshEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Integer> {
    Boolean existsByRefresh(String refresh);

    //원래는 repository 내에서 @Transactional을 달아주면 안되고 기능을 구현하는 service 에서 이를 달아주어야함
    @Transactional
    void deleteByRefresh(String refresh);
}
