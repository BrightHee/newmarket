package com.newmarket.notification;

import com.newmarket.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByAccountAndCheckedOrderByCreatedLocalDateTimeDesc(Account account, boolean checked);

    long countByAccountAndChecked(Account account, boolean checked);

    @Transactional
    void deleteByAccountAndChecked(Account account, boolean checked);

}
