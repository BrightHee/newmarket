package com.newmarket.main;

import com.newmarket.account.Account;
import com.newmarket.account.annotation.AuthenticatedAccount;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@AuthenticatedAccount Account account, Model model) {
        if (account != null) {
            model.addAttribute("account", account);
        }
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "account/login";
    }
}
