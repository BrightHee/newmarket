package com.newmarket.main;

import com.newmarket.account.Account;
import com.newmarket.account.annotation.AuthenticatedAccount;
import com.newmarket.garment.Garment;
import com.newmarket.garment.GarmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final GarmentRepository garmentRepository;

    @GetMapping("/")
    public String home(@AuthenticatedAccount Account account, Model model) {
        if (account != null) {
            model.addAttribute(account);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "account/login";
    }

    @GetMapping("/search")
    public String search(@PageableDefault(size = 20, sort = "updatedDateTime", direction = Sort.Direction.DESC) Pageable pageable,
                         @AuthenticatedAccount Account account, Model model, String keywords) {
        if (account != null) {
            model.addAttribute(account);
        }
        List<String> keywordList = Arrays.asList(keywords.split(" "));
        Page<Garment> currentGarments = garmentRepository.findByKeywords(pageable, keywordList);
        model.addAttribute("currentGarments", currentGarments);
        model.addAttribute("sortProperty", "updatedDateTime");
        model.addAttribute("keywords", keywords);
        return "search";
    }
}
