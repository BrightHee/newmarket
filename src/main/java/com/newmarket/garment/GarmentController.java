package com.newmarket.garment;

import com.newmarket.account.Account;
import com.newmarket.account.annotation.AuthenticatedAccount;
import com.newmarket.area.AreaRepository;
import com.newmarket.area.CityProvince;
import com.newmarket.garment.form.CityCountryDistrictForm;
import com.newmarket.garment.form.CityProvinceForm;
import com.newmarket.garment.form.DetailSearchForm;
import com.newmarket.garment.form.GarmentForm;
import com.newmarket.garment.validator.DetailSearchFormValidator;
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
    private final DetailSearchFormValidator detailSearchFormValidator;

    @InitBinder("garmentForm")
    public void validateGarmentForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(garmentFormValidator);
    }

    @InitBinder("detailSearchForm")
    public void validateDetailSearchForm(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(detailSearchFormValidator);
    }

    @ModelAttribute("account")
    public Account account(@AuthenticatedAccount Account account) {
        return account;
    }

    @GetMapping("/new-garment")
    public String newGarmentForm(@AuthenticatedAccount Account account, Model model, RedirectAttributes attributes) {
        if (!account.isEmailVerified()) {
            attributes.addFlashAttribute("errorMessage", "이메일 인증을 거쳐야 해당 서비스를 이용하실 수 있습니다.");
            return "redirect:/";
        }
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
            model.addAttribute("errorMessage", "글 작성에 실패했습니다.");
            model.addAttribute("garmentType", Stream.of(GarmentType.values()).map(Enum::name).collect(Collectors.toList()));
            model.addAttribute("cityProvinceList", cityProvince.getCityProvinceList());
            return "garment/new-garment";
        }
        garmentService.addNewGarment(garmentForm, account);
        attributes.addFlashAttribute("successMessage", "성공적으로 처리했습니다.");
        attributes.addFlashAttribute("detailSearchForm", new DetailSearchForm());
        return "redirect:/garments";
    }

    @PostMapping("/garment/area/cityCountryDistrict")
    @ResponseBody
    public ResponseEntity sendCityCountryDistrictInfo(@Valid @RequestBody CityProvinceForm cityProvinceForm) {
        String cityProvince = cityProvinceForm.getCityProvince();
        List<String> list = areaRepository.findDistinctCityCountryDistrict(cityProvince);
        return ResponseEntity.ok().header("Content-Type", "application/json; charset=utf-8").body(list);
    }

    @PostMapping("/garment/area/townTownshipNeighborhood")
    @ResponseBody
    public ResponseEntity sendTownTownshipNeighborhood(@Valid @RequestBody CityCountryDistrictForm cityCountryDistrictForm) {
        String cityProvince = cityCountryDistrictForm.getCityProvince();
        String cityCountryDistrict = cityCountryDistrictForm.getCityCountryDistrict();
        List<String> list = areaRepository.findTownTownshipNeighborhood(cityProvince, cityCountryDistrict);
        return ResponseEntity.ok().header("Content-Type", "application/json; charset=utf-8").body(list);
    }

    @GetMapping("/garments")
    public String showGarments(@PageableDefault(size = 20, sort = "updatedDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                               Model model, @Valid DetailSearchForm detailSearchForm, Errors errors) {
        model.addAttribute("cityProvinceList", cityProvince.getCityProvinceList());
        if (errors.hasErrors()) {
            model.addAttribute("errorMessage", "잘못된 요청입니다.");
            model.addAttribute("detailSearchForm", new DetailSearchForm());
            return "garment/garments";
        }

        model.addAttribute("cityCountryDistrictList",
                areaRepository.findDistinctCityCountryDistrict(detailSearchForm.getCityProvince()));
        model.addAttribute("townTownshipNeighborhoodList",
                areaRepository.findTownTownshipNeighborhood(detailSearchForm.getCityProvince(), detailSearchForm.getCityCountryDistrict()));
        Page<Garment> currentGarments = garmentRepository.findCurrentGarments(pageable, detailSearchForm);
        model.addAttribute("sortProperty", "updatedDateTime");
        model.addAttribute("currentGarments", currentGarments);
        return "garment/garments";
    }

    @GetMapping("/garment/{id}")
    public String showDetails(@PathVariable Long id, Model model) {
        Garment garment = garmentRepository.findById(id).orElseThrow();
        model.addAttribute("garment", garment);
        return "garment/details";
    }

    @GetMapping("/garments/management")
    public String manageGarments(@AuthenticatedAccount Account account, Model model) {
        List<Garment> garments = garmentRepository.findByAccountAndClosed(account, false);
        model.addAttribute("garments", garments);
        return "garment/management";
    }

    @GetMapping("/garment/{id}/update")
    public String updateGarmentForm(@PathVariable Long id, @AuthenticatedAccount Account account, Model model) {
        Garment garment = garmentRepository.findById(id).orElseThrow();
        if (!garmentService.checkIfValidAccessToModify(garment, account)) {
            model.addAttribute("errorMessage", "잘못된 접근입니다.");
            return "garment/management";
        }
        GarmentForm garmentForm = GarmentForm.builder()
                .title(garment.getTitle())
                .content(garment.getContent())
                .image(garment.getImage())
                .type(garment.getType().toString())
                .price(garment.getPrice())
                .cityProvince(garment.getArea().getCityProvince())
                .cityCountryDistrict(garment.getArea().getCityCountryDistrict())
                .townTownshipNeighborhood(garment.getArea().getTownTownshipNeighborhood())
                .build();
        model.addAttribute("garmentForm", garmentForm);
        model.addAttribute("garmentType", Stream.of(GarmentType.values()).map(Enum::name).collect(Collectors.toList()));
        model.addAttribute("cityProvinceList", cityProvince.getCityProvinceList());
        model.addAttribute("cityCountryDistrictList",
                areaRepository.findDistinctCityCountryDistrict(garmentForm.getCityProvince()));
        model.addAttribute("townTownshipNeighborhoodList",
                areaRepository.findTownTownshipNeighborhood(garmentForm.getCityProvince(), garmentForm.getCityCountryDistrict()));
        return "garment/update";
    }

    @PostMapping("/garment/{id}/update")
    public String updateGarment(@PathVariable Long id, @Valid GarmentForm garmentForm, Errors errors,
                                @AuthenticatedAccount Account account, Model model, RedirectAttributes attributes) {
        Garment garment = garmentRepository.findWithAccountById(id).orElseThrow();
        if (!garmentService.checkIfValidAccessToModify(garment, account)) {
            model.addAttribute("errorMessage", "잘못된 접근입니다.");
            return "garment/management";
        }
        if (errors.hasErrors()) {
            model.addAttribute("errorMessage", "글 수정에 실패했습니다.");
            model.addAttribute("cityProvinceList", cityProvince.getCityProvinceList());
            model.addAttribute("cityCountryDistrictList",
                    areaRepository.findDistinctCityCountryDistrict(garmentForm.getCityProvince()));
            model.addAttribute("townTownshipNeighborhoodList",
                    areaRepository.findTownTownshipNeighborhood(garmentForm.getCityProvince(), garmentForm.getCityCountryDistrict()));
            model.addAttribute("garmentType", Stream.of(GarmentType.values()).map(Enum::name).collect(Collectors.toList()));
            return "garment/update";
        }
        garmentService.updateGarment(garment, garmentForm);
        attributes.addFlashAttribute("successMessage", "성공적으로 글을 수정했습니다.");
        return "redirect:/garments/management";
    }

    @PostMapping("/garment/{id}/delete")
    public String deleteGarment(@PathVariable Long id, @AuthenticatedAccount Account account, Model model,
                                RedirectAttributes attributes) {
        Garment garment = garmentRepository.findWithAccountById(id).orElseThrow();
        if (!garmentService.checkIfValidAccessToModify(garment, account)) {
            model.addAttribute("errorMessage", "잘못된 접근입니다.");
            return "garment/management";
        }
        garmentService.deleteGarment(garment);
        attributes.addFlashAttribute("successMessage", "성공적으로 글을 삭제했습니다.");
        return "redirect:/garments/management";
    }

}
