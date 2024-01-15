package com.example.quickpay.repository;


import com.example.quickpay.domain.Account;
import com.example.quickpay.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findFirstByOrderByIdDesc();

    Integer countByAccountUser(Member account);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByAccountUser(Member member);

}
