package com.newmarket.garment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface GarmentRepositoryExtension {

    Page<Garment> findCurrentGarments(Pageable pageable);

}
