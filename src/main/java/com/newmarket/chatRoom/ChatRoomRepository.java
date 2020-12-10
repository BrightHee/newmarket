package com.newmarket.chatRoom;

import com.newmarket.account.Account;
import com.newmarket.garment.Garment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @EntityGraph(attributePaths = { "chatList" })
    ChatRoom findBySellerAndBuyerAndGarment(Account seller, Account buyer, Garment garment);

    List<ChatRoom> findByGarmentAndSeller(Garment garment, Account seller);

}
