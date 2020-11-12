package com.newmarket.garment;

import com.newmarket.area.QArea;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class GarmentRepositoryExtensionImpl extends QuerydslRepositorySupport implements GarmentRepositoryExtension {

    public GarmentRepositoryExtensionImpl() {
        super(Garment.class);
    }

    @Override
    public Page<Garment> findCurrentGarments(Pageable pageable) {
        QGarment garment = QGarment.garment;
        JPQLQuery<Garment> query = from(garment)
                .leftJoin(garment.area, QArea.area).fetchJoin();
        JPQLQuery<Garment> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Garment> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

}
