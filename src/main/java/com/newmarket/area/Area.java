package com.newmarket.area;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Area {

    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String cityOrProvince;

    @Column(nullable = false)
    private String cityOrCountryOrDistrict;

    @Column(nullable = false)
    private String townOrTownshipOrNeighborhood;

}
