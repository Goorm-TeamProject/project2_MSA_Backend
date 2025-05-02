package com.eouil.msa.money.account.kafka;

import com.eouil.msa.money.account.dtos.CreateAccountRequest;
import com.eouil.msa.money.account.services.AccountService;
import com.eouil.msa.shared.kafka.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventListener {

    private final AccountService accountService;

    @KafkaListener(topics = "user.created", groupId = "account-service")
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("[Kafka] 사용자 생성 이벤트 수신 - userId: {}", event.getUserId());

        CreateAccountRequest req = new CreateAccountRequest(BigDecimal.ZERO);
        accountService.createAccount(req, event.getUserId());

        log.info("[Kafka] 계좌 생성 완료 - userId: {}", event.getUserId());
    }
}