package com.newmarket.account.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationForm {

    private boolean sentSellerChatMessages;

    private boolean sentBuyerChatMessages;

}
