package com.newmarket.garment;

import com.newmarket.account.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface GarmentRepository extends JpaRepository<Garment, Long>, GarmentRepositoryExtension {

    List<Garment> findByTitle(String title);

    List<Garment> findByAccountAndClosed(Account account, boolean closed);

    @EntityGraph(attributePaths = { "account" })
    Optional<Garment> findWithAccountById(Long id);

}
