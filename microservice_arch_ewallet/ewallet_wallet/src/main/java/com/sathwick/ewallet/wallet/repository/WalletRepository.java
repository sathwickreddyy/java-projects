package com.sathwick.ewallet.wallet.repository;

import com.sathwick.ewallet.wallet.domain.Wallet;
import org.hibernate.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long>{
    Optional<Wallet> findByUserId(Long userId);
}
