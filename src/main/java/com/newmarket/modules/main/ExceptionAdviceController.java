package com.newmarket.modules.main;

import com.newmarket.modules.account.Account;
import com.newmarket.modules.account.annotation.AuthenticatedAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class ExceptionAdviceController {

    @ExceptionHandler
    public String handleRuntimeException(@AuthenticatedAccount Account account, HttpServletRequest req, RuntimeException e) {
        if (account != null) {
            log.info("'{}' requested '{}'", account.getNickname(), req.getRequestURI());
        } else {
            log.info("requested '{}'", req.getRequestURI());
        }
        log.error("bad request", e);
        return "error";
    }

}
