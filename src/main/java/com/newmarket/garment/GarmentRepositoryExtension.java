package com.newmarket.garment;

import com.newmarket.garment.form.DetailSearchForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface GarmentRepositoryExtension {

    Page<Garment> findCurrentGarments(Pageable pageable, DetailSearchForm detailSearchForm);

    Page<Garment> findByKeywords(Pageable pageable, List<String> keywordList);

}
