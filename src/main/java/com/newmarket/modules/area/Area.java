package com.newmarket.modules.area;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Area {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String cityProvince;

    @Column(nullable = false)
    private String cityCountryDistrict;

    @Column(nullable = false)
    private String townTownshipNeighborhood;

}
