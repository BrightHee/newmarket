package com.newmarket.modules.garment;

import com.newmarket.modules.garment.form.DetailSearchForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface GarmentRepositoryExtension {

    Page<Garment> findCurrentGarments(Pageable pageable, DetailSearchForm detailSearchForm);

    Page<Garment> findByKeywords(Pageable pageable, List<String> keywordList);

}
