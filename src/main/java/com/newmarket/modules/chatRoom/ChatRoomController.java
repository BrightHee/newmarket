package com.newmarket.modules.chatRoom;

import com.newmarket.modules.account.Account;
import com.newmarket.modules.account.AccountRepository;
import com.newmarket.modules.account.annotation.AuthenticatedAccount;
import com.newmarket.modules.chatRoom.form.MessageForm;
import com.newmarket.modules.garment.Garment;
import com.newmarket.modules.garment.GarmentRepository;
import com.newmarket.modules.garment.form.ChatPartnerForm;
import com.newmarket.modules.garment.form.ChatRoomForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/garment/{id}")
public class ChatRoomController {

    private final GarmentRepository garmentRepository;
    private final AccountRepository accountRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomService chatRoomService;

    @ModelAttribute("account")
    public Account account(@AuthenticatedAccount Account account) {
        return account;
    }

    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity sendChatMessage(@PathVariable Long id, @Valid @RequestBody ChatRoomForm chatRoomForm,
                                          @AuthenticatedAccount Account account) {
        Garment garment = garmentRepository.findWithAccountById(id).orElseThrow();
        Account seller = accountRepository.findByNickname(chatRoomForm.getSellerNickname());
        Account buyer = accountRepository.findByNickname(chatRoomForm.getBuyerNickname());
        if (!seller.canChatFor(buyer) || garment.isClosed()) {
            return ResponseEntity.badRequest().build();
        }
        ChatRoom chatRoom = chatRoomRepository.findBySellerAndBuyerAndGarment(seller, buyer, garment);
        if (chatRoom == null || !chatRoom.hasMemberOf(account)) {
            return ResponseEntity.badRequest().build();
        }
        Chat chat = chatRoomService.addNewChatMessage(chatRoomForm, chatRoom, account);
        MessageForm messageForm = MessageForm.builder().nickname(chat.getSender().getNickname())
                .me(true).message(chat.getMessage()).sentDateTime(chat.getSentDateTime()).build();
        return ResponseEntity.ok().header("Content-Type", "application/json; charset=utf-8")
                .body(messageForm);
    }

    @PostMapping("/chatList")
    @ResponseBody
    public ResponseEntity getChatList(@PathVariable Long id, @Valid @RequestBody ChatPartnerForm chatPartnerForm,
                                      @AuthenticatedAccount Account account) {
        Garment garment = garmentRepository.findWithAccountById(id).orElseThrow();
        Account partner = accountRepository.findByNickname(chatPartnerForm.getNickname());
        if (!account.canChatFor(partner)) {
            return ResponseEntity.badRequest().build();
        }
        ChatRoom chatRoom = chatRoomService.findOrCreateNew(account, partner, chatPartnerForm.getBuyerOrSeller(), garment);
        if (chatRoom == null) {
            return ResponseEntity.badRequest().build();
        }
        List<MessageForm> chatList = chatRoom.getChatList().stream().map(chat -> MessageForm.builder()
                .nickname(chat.getSender().getNickname()).me(chat.getSender().equals(account)).message(chat.getMessage())
                .sentDateTime(chat.getSentDateTime()).build()).collect(Collectors.toList());
        return ResponseEntity.ok().header("Content-Type", "application/json; charset=utf-8")
                .body(chatList);
    }

}
