package com.example.quickpay.domain.mysql.repository;

import com.example.quickpay.domain.mysql.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
}
