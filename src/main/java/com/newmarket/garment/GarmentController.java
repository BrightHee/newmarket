package com.newmarket.garment;

import com.newmarket.account.Account;
import com.newmarket.account.annotation.AuthenticatedAccount;
import com.newmarket.area.AreaRepository;
import com.newmarket.area.CityProvince;
import com.newmarket.garment.form.CityCountryDistrictForm;
import com.newmarket.garment.form.CityProvinceForm;
import com.newmarket.garment.form.GarmentForm;
import com.newmarket.garment.validator.GarmentFormValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
public class GarmentController {

    private final GarmentRepository garmentRepository;
    private final GarmentService garmentService;
    private final CityProvince cityProvince;
    private final AreaRepository areaRepository;
    private final GarmentFormValidator garmentFormValidator;

    @InitBinder("garmentForm")
    public void validateGarmentForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(garmentFormValidator);
    }

    @GetMapping("/new-garment")
    public String newGarmentForm(@AuthenticatedAccount Account account, Model model, RedirectAttributes attributes) {
        if (!account.isEmailVerified()) {
            attributes.addFlashAttribute("errorMessage", "이메일 인증을 거쳐야 해당 서비스를 이용하실 수 있습니다.");
            return "redirect:/";
        }
        model.addAttribute(account);
        model.addAttribute("garmentType", Stream.of(GarmentType.values()).map(Enum::name).collect(Collectors.toList()));
        model.addAttribute("cityProvinceList", cityProvince.getCityProvinceList());
        model.addAttribute("garmentForm", new GarmentForm());
        return "garment/new-garment";
    }

    @PostMapping("/new-garment")
    public String newGarment(@Valid GarmentForm garmentForm, Errors errors, Model model,
                             @AuthenticatedAccount Account account, RedirectAttributes attributes) {
        if (!account.isEmailVerified()) {
            attributes.addFlashAttribute("errorMessage", "이메일 인증을 거쳐야 해당 서비스를 이용하실 수 있습니다.");
            return "redirect:/";
        }
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute("errorMessage", "글 작성에 실패했습니다.");
            model.addAttribute("garmentType", Stream.of(GarmentType.values()).map(Enum::name).collect(Collectors.toList()));
            model.addAttribute("cityProvinceList", cityProvince.getCityProvinceList());
            return "garment/new-garment";
        }
        garmentService.addNewGarment(garmentForm, account);
        attributes.addFlashAttribute("successMessage", "성공적으로 처리했습니다.");
        return "redirect:/garments";
    }

    @PostMapping("/new-garment/cityCountryDistrict")
    @ResponseBody
    public ResponseEntity sendCityCountryDistrictInfo(@Valid @RequestBody CityProvinceForm cityProvinceForm) {
        String cityProvince = cityProvinceForm.getCityProvince();
        List<String> list = areaRepository.findDistinctCityCountryDistrict(cityProvince);
        return ResponseEntity.ok().header("Content-Type", "application/json; charset=utf-8").body(list);
    }

    @PostMapping("/new-garment/townTownshipNeighborhood")
    @ResponseBody
    public ResponseEntity sendTownTownshipNeighborhood(@Valid @RequestBody CityCountryDistrictForm cityCountryDistrictForm) {
        String cityProvince = cityCountryDistrictForm.getCityProvince();
        String cityCountryDistrict = cityCountryDistrictForm.getCityCountryDistrict();
        List<String> list = areaRepository.findTownTownshipNeighborhood(cityProvince, cityCountryDistrict);
        return ResponseEntity.ok().header("Content-Type", "application/json; charset=utf-8").body(list);
    }

    @GetMapping("/garments")
    public String showGarments(@PageableDefault(size = 20, sort = "updatedDateTime", direction = Sort.Direction.DESC) Pageable pageable, Model model,
                               @AuthenticatedAccount Account account) {
        if (account != null) {
            model.addAttribute(account);
        }
        Page<Garment> currentGarments = garmentRepository.findCurrentGarments(pageable);
        model.addAttribute("currentGarments", currentGarments);
        return "garment/garments";
    }

}
