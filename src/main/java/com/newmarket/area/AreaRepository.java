package com.newmarket.area;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface AreaRepository extends JpaRepository<Area, Long> {

    @Query("SELECT DISTINCT cityProvince FROM Area")
    List<String> findDistinctCityProvince();

    @Query("SELECT DISTINCT cityCountryDistrict FROM Area WHERE cityProvince= ?1")
    List<String> findDistinctCityCountryDistrict(String cityProvince);

    @Query("SELECT townTownshipNeighborhood FROM Area WHERE cityProvince= ?1 and cityCountryDistrict= ?2")
    List<String> findTownTownshipNeighborhood(String cityProvince, String cityCountryDistrict);

    boolean existsByCityProvinceAndCityCountryDistrictAndTownTownshipNeighborhood(String cityProvince, String cityCountryDistrict, String townTownshipNeighborhood);

    Area findByCityProvinceAndCityCountryDistrictAndTownTownshipNeighborhood(String cityProvince, String cityCountryDistrict, String townTownshipNeighborhood);

}
