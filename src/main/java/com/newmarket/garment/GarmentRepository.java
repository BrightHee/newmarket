package com.newmarket.garment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface GarmentRepository extends JpaRepository<Garment, Long>, GarmentRepositoryExtension {
}
