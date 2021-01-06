package com.newmarket.modules.garment;

import com.newmarket.modules.account.Account;
import com.newmarket.modules.garment.GarmentService;
import com.newmarket.modules.garment.GarmentType;
import com.newmarket.modules.garment.form.GarmentForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GarmentFactory {

    @Autowired
    GarmentService garmentService;

    public void createGarment(int num, Account account, String cityProvince, String cityCountryDistrict, String TownTownshipNeighborhood) throws InterruptedException {
        GarmentForm garmentForm = new GarmentForm();
        for (int i = 0; i < num; i++) {
            garmentForm.setTitle("제목(" + (i+1) + ")");
            garmentForm.setType(GarmentType.values()[(i+1) % GarmentType.values().length].toString());
            garmentForm.setPrice((i+1) * 1000);
            garmentForm.setCityProvince(cityProvince);
            garmentForm.setCityCountryDistrict(cityCountryDistrict);
            garmentForm.setTownTownshipNeighborhood(TownTownshipNeighborhood);
            garmentForm.setContent("내용~~!");
            garmentService.addNewGarment(garmentForm, account);
            Thread.sleep(10);  // saveAll로 한번에 저장하는 것 대신에 간격을 두고 저장
        }
    }

}
