package com.newmarket.garment;

import com.newmarket.area.QArea;
import com.newmarket.garment.form.DetailSearchForm;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.time.LocalDateTime;

public class GarmentRepositoryExtensionImpl extends QuerydslRepositorySupport implements GarmentRepositoryExtension {

    QGarment garment = QGarment.garment;

    public GarmentRepositoryExtensionImpl() {
        super(Garment.class);
    }

    @Override
    public Page<Garment> findCurrentGarments(Pageable pageable, DetailSearchForm detailSearchForm) {
        String cityProvince = detailSearchForm.getCityProvince();
        String cityCountryDistrict = detailSearchForm.getCityCountryDistrict();
        String townTownshipNeighborhood = detailSearchForm.getTownTownshipNeighborhood();

        JPQLQuery<Garment> query = from(garment)
                .where(eqClosed(detailSearchForm.getClosed()),
                    innerDuration(detailSearchForm.getDuration()),
                    eqCityProvince(cityProvince),
                    eqCityCountryDistrict(cityProvince, cityCountryDistrict),
                    eqTownTownshipNeighborhood(cityProvince, cityCountryDistrict, townTownshipNeighborhood))
                .leftJoin(garment.area, QArea.area).fetchJoin();
        JPQLQuery<Garment> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<Garment> fetchResults = pageableQuery.fetchResults();
        return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
    }

    private BooleanExpression eqClosed(String closed) {
        if (closed.equals("전체")) {
            return null;
        }

        return garment.closed.isFalse();
    }

    private BooleanExpression innerDuration(String durationString) {
        LocalDateTime duration = null;
        switch (durationString) {
            case "오늘":
                duration = LocalDateTime.now().minusDays(1);
                break;
            case "이번주":
                duration = LocalDateTime.now().minusWeeks(1);
                break;
            case "1개월":
                duration = LocalDateTime.now().minusMonths(1);
                break;
            default:
                duration = LocalDateTime.now().minusMonths(6);
        }

        return garment.updatedDateTime.after(duration);
    }

    private BooleanExpression eqCityProvince(String cityProvince) {
        if (cityProvince.equals("전체")) {
            return null;
        }
        return garment.area.cityProvince.eq(cityProvince);
    }

    private BooleanExpression eqCityCountryDistrict(String cityProvince, String cityCountryDistrict) {
        if (cityProvince.equals("전체") || cityCountryDistrict.equals("전체")) {
            return null;
        }
        return garment.area.cityCountryDistrict.eq(cityCountryDistrict);
    }

    private BooleanExpression eqTownTownshipNeighborhood(String cityProvince, String cityCountryDistrict, String townTownshipNeighborhood) {
        if (cityProvince.equals("전체") || cityCountryDistrict.equals("전체") || townTownshipNeighborhood.equals("전체")) {
            return null;
        }
        return garment.area.townTownshipNeighborhood.eq(townTownshipNeighborhood);
    }

}
