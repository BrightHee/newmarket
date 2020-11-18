package com.newmarket.garment;

import com.newmarket.account.Account;
import com.newmarket.area.Area;
import com.newmarket.area.AreaRepository;
import com.newmarket.garment.form.GarmentForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class GarmentService {

    private final GarmentRepository garmentRepository;
    private final AreaRepository areaRepository;

    public void addNewGarment(GarmentForm garmentForm, Account account) {
        Area area = areaRepository.findByCityProvinceAndCityCountryDistrictAndTownTownshipNeighborhood(
                garmentForm.getCityProvince(), garmentForm.getCityCountryDistrict(), garmentForm.getTownTownshipNeighborhood());
        Garment newGarment = Garment.builder()
                .title(garmentForm.getTitle())
                .content(garmentForm.getContent())
                .image(garmentForm.getImage())
                .type(GarmentType.valueOf(garmentForm.getType()))
                .price(garmentForm.getPrice())
                .account(account)
                .area(area)
                .updatedDateTime(LocalDateTime.now())
                .build();
        garmentRepository.save(newGarment);
    }

    public void deleteGarment(Garment garment) {
        garmentRepository.delete(garment);
    }

    public void updateGarment(Garment garment, GarmentForm garmentForm) {
        Area area = areaRepository.findByCityProvinceAndCityCountryDistrictAndTownTownshipNeighborhood(
                garmentForm.getCityProvince(), garmentForm.getCityCountryDistrict(), garmentForm.getTownTownshipNeighborhood());
        garment.setTitle(garmentForm.getTitle());
        garment.setContent(garmentForm.getContent());
        garment.setImage(garmentForm.getImage());
        garment.setPrice(garmentForm.getPrice());
        garment.setType(GarmentType.valueOf(garmentForm.getType()));
        garment.setArea(area);
        garment.setUpdatedDateTime(LocalDateTime.now());
        garmentRepository.save(garment);
    }

    public boolean checkIfValidAccess(Garment garment, Account account) {
        return !garment.isClosed() && garment.getAccount().getEmail().equals(account.getEmail());
    }
}
