package com.newmarket.modules.area;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter @Setter
public class CityProvince {

    private List<String> cityProvinceList = new ArrayList<>();

}
